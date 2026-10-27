param(
    [Parameter(Mandatory=$true)][ValidatePattern('^0[0-9]{9,13}$')][string]$Phone,
    [Parameter(Mandatory=$true)][ValidatePattern('^[0-9]{6}$')][string]$Pin,
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path $PSScriptRoot -Parent
$privatePath = Join-Path $projectRoot 'private'
New-Item -ItemType Directory -Force $privatePath | Out-Null

function Invoke-Adb {
    $adbArguments = @($args)
    & $Adb @adbArguments | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "ADB failed: $($adbArguments[0..([Math]::Min(2,$adbArguments.Length-1))] -join ' ')" }
}
function Get-Tree {
    for ($recovery=0;$recovery -lt 8;$recovery++) {
        $dumpMessage = (& $Adb shell timeout 25 uiautomator dump /data/local/tmp/rt-capture.xml 2>&1) -join "`n"
        if ($LASTEXITCODE -ne 0 -or $dumpMessage -notmatch 'UI hierchary dumped to') { Start-Sleep -Seconds 3; continue }
        Invoke-Adb @('pull','/data/local/tmp/rt-capture.xml',"$privatePath\capture-ui.xml")
        [xml]$tree = Get-Content -LiteralPath "$privatePath\capture-ui.xml" -Raw
        $close = $tree.SelectSingleNode('//node[@resource-id="android:id/aerr_close"]')
        if ($close) {
            $title = $tree.SelectSingleNode('//node[@resource-id="android:id/alertTitle"]').text
            if ($title -like '*RT Smart*') { throw "App ANR: $title" }
            Tap-Node $close
            Start-Sleep -Seconds 2
            continue
        }
        return $tree
    }
    throw 'Android system is not responding; do not capture misleading screenshots.'
}
function Tap-Node($Node) {
    if (-not $Node) { throw 'UI element not found' }
    $match = [regex]::Match($Node.bounds,'\[(\d+),(\d+)\]\[(\d+),(\d+)\]')
    if (-not $match.Success) { throw 'Invalid element bounds' }
    $x = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value)/2)
    $y = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value)/2)
    Invoke-Adb @('shell','input','tap',"$x","$y")
}
function Tap-Label([string]$Label) {
    $tree = Get-Tree
    $matches = $tree.SelectNodes("//node[@text='$Label']")
    $node = if ($matches.Count -gt 0) { $matches[$matches.Count-1] } else { $tree.SelectSingleNode("//node[@content-desc='$Label']") }
    if (-not $node) { throw "Missing UI label: $Label" }
    Tap-Node $node
    Start-Sleep -Seconds 2
}
function Enter-Value($Node,[string]$Value) {
    $passwordFlag = $Node.password
    Tap-Node $Node
    $focusedCorrectly=$false
    for ($focusAttempt=0;$focusAttempt -lt 6;$focusAttempt++) {
        Start-Sleep -Seconds 1
        $focusTree=Get-Tree
        $focused=$focusTree.SelectSingleNode('//node[@class="android.widget.EditText" and @focused="true"]')
        if ($focused -and $focused.password -eq $passwordFlag) { $focusedCorrectly=$true;break }
        $freshNode=$focusTree.SelectSingleNode("//node[@class='android.widget.EditText' and @password='$passwordFlag']")
        if ($freshNode) { Tap-Node $freshNode }
    }
    if (-not $focusedCorrectly) { throw 'Text field focus could not be verified; no credentials entered.' }
    if (-not [string]::IsNullOrEmpty($Node.text)) {
        Invoke-Adb @('shell','input','keycombination','113','29')
        Invoke-Adb @('shell','input','keyevent','67')
    }
    Invoke-Adb @('shell','input','text',$Value)
}
function Wait-Text([string]$Text) {
    for ($attempt=0;$attempt -lt 15;$attempt++) {
        $tree=Get-Tree
        if ($tree.OuterXml.Contains($Text)) { return }
        Start-Sleep -Seconds 2
    }
    throw "Expected UI text was not displayed: $Text"
}
function Save-Capture([string]$Device,[string]$Name) {
    $folder=Join-Path $projectRoot "captures\$Device"
    New-Item -ItemType Directory -Force $folder | Out-Null
    Invoke-Adb @('shell','cmd','statusbar','collapse')
    $rendered=$false
    for ($frameAttempt=0;$frameAttempt -lt 10;$frameAttempt++) {
        Invoke-Adb @('shell','screencap','-p','/data/local/tmp/rt-capture.png')
        Invoke-Adb @('pull','/data/local/tmp/rt-capture.png',"$folder\$Name.png")
        $bitmap=[System.Drawing.Bitmap]::new("$folder\$Name.png")
        try {
            foreach($point in @(@(.25,.25),@(.5,.5),@(.75,.75),@(.25,.75),@(.75,.25))) {
                $pixel=$bitmap.GetPixel([int]($bitmap.Width*$point[0]),[int]($bitmap.Height*$point[1]))
                if (($pixel.R+$pixel.G+$pixel.B) -gt 45) { $rendered=$true;break }
            }
        } finally { $bitmap.Dispose() }
        if ($rendered) { break }
        Invoke-Adb @('shell','input','keyevent','224')
        Start-Sleep -Seconds 3
    }
    if (-not $rendered) { throw "Black compositor frame for $Device/$Name; screenshot rejected." }
    Write-Output "Captured $Device/$Name"
}

Invoke-Adb @('shell','input','keyevent','224')
Invoke-Adb @('shell','settings','put','system','screen_off_timeout','2147483647')
Invoke-Adb @('shell','wm','dismiss-keyguard')
Invoke-Adb @('shell','settings','put','global','window_animation_scale','0')
Invoke-Adb @('shell','settings','put','global','transition_animation_scale','0')
Invoke-Adb @('shell','settings','put','global','animator_duration_scale','0')
$tree=$null
for ($startup=0;$startup -lt 20;$startup++) {
    $tree=Get-Tree
    $fields=$tree.SelectNodes('//node[@class="android.widget.EditText"]')
    if ($fields.Count -eq 2 -or $tree.SelectNodes('//node[@text="Beranda"]').Count -gt 0) { break }
    Start-Sleep -Seconds 2
}
if ($fields.Count -eq 2) {
    Write-Output 'Login form ready.'
    Enter-Value $fields[0] $Phone
    $phoneImeState = (& $Adb shell dumpsys input_method) -join "`n"
    if ($phoneImeState -match 'mInputShown=true|mIsInputViewShown=true') {
        Invoke-Adb @('shell','input','keyevent','4')
        Start-Sleep -Seconds 1
    }
    $tree=Get-Tree
    $pinField=$tree.SelectSingleNode('//node[@class="android.widget.EditText" and @password="true"]')
    if (-not $pinField) { $pinField=$tree.SelectNodes('//node[@class="android.widget.EditText"]')[1] }
    Enter-Value $pinField $Pin
    Write-Output 'Phone and PIN entered into verified fields.'
    $imeState = (& $Adb shell dumpsys input_method) -join "`n"
    if ($imeState -match 'mInputShown=true|mIsInputViewShown=true') { Invoke-Adb @('shell','input','keyevent','4'); Start-Sleep -Seconds 3 }
    Tap-Label 'Masuk ke lingkungan Anda'
}
if ($fields.Count -ne 2) { Tap-Label 'Beranda' }
Wait-Text 'Iuran lingkungan Anda'
Write-Output 'Live login verified.'
foreach ($device in @('phone','tablet-7','tablet-10','desktop')) {
    switch ($device) {
        'phone' { $size='720x1600';$density='280';$rotation='0' }
        'tablet-7' { $size='800x1280';$density='160';$rotation='0' }
        'tablet-10' { $size='800x1280';$density='134';$rotation='0' }
        'desktop' { $size='800x1280';$density='107';$rotation='0' }
    }
    Invoke-Adb @('shell','settings','put','system','accelerometer_rotation','0')
    Invoke-Adb @('shell','wm','size',$size)
    Invoke-Adb @('shell','wm','density',$density)
    Invoke-Adb @('shell','settings','put','system','user_rotation',$rotation)
    Start-Sleep -Seconds 5
    foreach ($page in @(
        @{label='Beranda';name='home';expected='Iuran lingkungan Anda'},
        @{label='IPL';name='ipl';expected='Iuran tertib.'},
        @{label='Kabar RT';name='news';expected='Kabar baik'},
        @{label='Kas';name='finance';expected='Kas terbuka.'}
    )) {
        Tap-Label $page.label
        Wait-Text $page.expected
        Start-Sleep -Seconds 2
        Save-Capture $device $page.name
    }
    if ($device -eq 'phone') {
        Tap-Label 'Lainnya'
        Tap-Label 'Data warga'
        Wait-Text 'Tetangga kita.'
        Save-Capture $device 'residents'
    }
}
Write-Output 'All 17 native screenshots captured.'
