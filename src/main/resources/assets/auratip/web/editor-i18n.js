// -----------------------------
// i18n (loaded from /lang/*.json)
// -----------------------------

const I18N = {
  en: {
    // minimal fallback (real strings live in web/lang/en.json)
    connecting: 'Connecting…',
    connected: 'Connected',
    disconnected: 'Disconnected'
  },
  zh: {
    // minimal fallback (real strings live in web/lang/zh.json)
    connecting: '连接中…',
    connected: '已连接',
    disconnected: '已断开'
  }
};

async function loadLangFile(lang) {
  try {
    const res = await fetch(`lang/${lang}.json`, { cache: 'no-store' });
    if (!res.ok) return;
    const data = await res.json();
    if (data && typeof data === 'object') {
      I18N[lang] = { ...(I18N[lang] || {}), ...data };
    }
  } catch {
    // ignore (fallback stays)
  }
}

let LANG = localStorage.getItem('auratip_lang') || (navigator.language && navigator.language.toLowerCase().startsWith('zh') ? 'zh' : 'en');
if (!I18N[LANG]) LANG = 'en';

function t(key) {
  return (I18N[LANG] && I18N[LANG][key]) || I18N.en[key] || key;
}

function applyI18n() {
  try { document.documentElement.lang = (LANG === 'zh') ? 'zh-CN' : 'en'; } catch {}
  // static texts
  if (previewLine1) previewLine1.textContent = t('preview_line1');
  if (previewLine2) previewLine2.textContent = t('preview_line2');

  const tipCardTitle = document.getElementById('tipCardTitle');
  const tipCardDesc = document.getElementById('tipCardDesc');
  const radialCardTitle = document.getElementById('radialCardTitle');
  const radialCardDesc = document.getElementById('radialCardDesc');
  if (tipCardTitle) tipCardTitle.textContent = t('tip_card_title');
  if (tipCardDesc) tipCardDesc.textContent = t('tip_card_desc');
  if (radialCardTitle) radialCardTitle.textContent = t('radial_card_title');
  if (radialCardDesc) radialCardDesc.textContent = t('radial_card_desc');

  const panelSub = document.getElementById('panelSub');
  if (panelSub) panelSub.textContent = t('panel_sub');
  btnSendNow.textContent = t('btn_send_now');

  // buttons / titles
  btnToggleAnim.title = t('anim_title');
  btnExport.title = t('export_title');
  btnCopyJson.title = t('copy_json');
  btnClose.title = t('back');

  exportJson.textContent = t('export_json');
  exportJs.textContent = t('export_js');
  exportJava.textContent = t('export_java');

  // animation panel
  const animTitle = document.getElementById('animTitle');
  const animSub = document.getElementById('animSub');
  const animFooterHelp = document.getElementById('animFooterHelp');
  if (animTitle) animTitle.textContent = t('anim_title');
  if (animSub) animSub.textContent = t('anim_sub');
  btnRunAnim.textContent = t('anim_run');
  if (animFooterHelp) animFooterHelp.textContent = t('anim_footer_help');
  btnCopyAnimJava.textContent = t('anim_copy_java');
  if (animId) animId.placeholder = t('anim_id_placeholder');
  if (btnUseDefaultsAsTest) btnUseDefaultsAsTest.textContent = t('anim_use_defaults');

  const animIdLabel = document.getElementById('animIdLabel');
  const animIdHelp = document.getElementById('animIdHelp');
  const animDefaultsLabel = document.getElementById('animDefaultsLabel');
  const animDefaultsHelp = document.getElementById('animDefaultsHelp');
  const animTestParamsLabel = document.getElementById('animTestParamsLabel');
  const animTestParamsHelp = document.getElementById('animTestParamsHelp');
  if (animIdLabel) animIdLabel.textContent = t('anim_id_label');
  if (animIdHelp) animIdHelp.textContent = t('anim_id_help');
  if (animDefaultsLabel) animDefaultsLabel.textContent = t('anim_defaults_label');
  if (animDefaultsHelp) animDefaultsHelp.textContent = t('anim_defaults_help');
  if (animTestParamsLabel) animTestParamsLabel.textContent = t('anim_test_params_label');
  if (animTestParamsHelp) animTestParamsHelp.textContent = t('anim_test_params_help');

  // json inspector
  const jsonTitle = document.getElementById('jsonTitle');
  const jsonSub = document.getElementById('jsonSub');
  if (jsonTitle) jsonTitle.textContent = t('json_title');
  if (jsonSub) jsonSub.textContent = t('json_sub');
  btnJson.title = t('json_title');
  btnJsonApply.textContent = t('json_apply');
  btnJsonSync.textContent = t('json_sync');
  btnJsonRevert.textContent = t('json_revert');
  btnJsonCopy.textContent = t('json_copy');

  // connection pill baseline (state-specific strings set via setConn)
}
