#!/usr/bin/env bash
set -euo pipefail
runner_dir=/home/ubuntu/rtsmartpro-runner
mkdir -p "$runner_dir"
cd "$runner_dir"
if [ ! -f .runner ]; then
  curl -fLsS -o runner.tar.gz https://github.com/actions/runner/releases/download/v2.338.1/actions-runner-linux-arm64-2.338.1.tar.gz
  tar xzf runner.tar.gz
  sudo ./bin/installdependencies.sh
  ./config.sh --url https://github.com/seandyadryan/rt-smart-online --token "$(cat /opt/rtsmartpro/runner-token.txt)" --name rtsmartpro-ubuntu-arm64 --labels rtsmartpro --work _work --unattended
  sudo ./svc.sh install ubuntu
  sudo ./svc.sh start
fi
rm -f /opt/rtsmartpro/runner-token.txt
