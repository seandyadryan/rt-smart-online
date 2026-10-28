(() => {
  const featureTabs = [...document.querySelectorAll('[data-feature]')];
  function selectFeature(id, focus = false) {
    const selected = featureTabs.find(tab => tab.dataset.feature === id) || featureTabs[0];
    if (!selected) return;
    featureTabs.forEach(tab => {
      const active = tab === selected;
      tab.setAttribute('aria-selected', String(active));
      tab.tabIndex = active ? 0 : -1;
      document.getElementById(tab.getAttribute('aria-controls')).hidden = !active;
    });
    if (focus) selected.focus();
  }
  featureTabs.forEach((tab, i) => {
    tab.addEventListener('click', () => {
      selectFeature(tab.dataset.feature);
      history.replaceState(null, '', '#' + tab.dataset.feature);
    });
    tab.addEventListener('keydown', event => {
      const offsets = { ArrowRight: 1, ArrowLeft: -1 };
      let index;
      if (event.key in offsets) index = (i + offsets[event.key] + featureTabs.length) % featureTabs.length;
      else if (event.key === 'Home') index = 0;
      else if (event.key === 'End') index = featureTabs.length - 1;
      else return;
      event.preventDefault();
      selectFeature(featureTabs[index].dataset.feature, true);
      history.replaceState(null, '', '#' + featureTabs[index].dataset.feature);
    });
  });
  if (featureTabs.length) selectFeature(location.hash.slice(1));
  window.addEventListener('hashchange', () => selectFeature(location.hash.slice(1)));

  const stepLinks = [...document.querySelectorAll('[data-step]')];
  const stepPanels = [...document.querySelectorAll('.step-panel')];
  function selectStep(index) {
    if (!Number.isInteger(index) || index < 0 || index >= stepPanels.length) index = 0;
    stepPanels.forEach((panel, i) => { panel.hidden = i !== index; });
    document.querySelectorAll('.step-tabs [data-step]').forEach(link => {
      if (Number(link.dataset.step) === index) link.setAttribute('aria-current', 'step');
      else link.removeAttribute('aria-current');
    });
  }
  stepLinks.forEach(link => link.addEventListener('click', event => {
    event.preventDefault();
    const index = Number(link.dataset.step);
    selectStep(index);
    history.replaceState(null, '', '#langkah-' + (index + 1));
    if (!link.closest('.step-tabs')) {
      const title = document.querySelector('#step-title-' + (index + 1));
      title.tabIndex = -1;
      title.focus({ preventScroll: true });
    }
  }));
  if (stepPanels.length) selectStep(Number(location.hash.replace('#langkah-', '')) - 1);
  window.addEventListener('hashchange', () => {
    if (stepPanels.length) selectStep(Number(location.hash.replace('#langkah-', '')) - 1);
  });
})();
