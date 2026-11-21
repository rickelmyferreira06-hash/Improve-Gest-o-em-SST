<?php
/**
 * Pagina PUBLICA de download do APK Improve SST.
 * Instale este arquivo em https://sistema.improveengenhariasst.com.br/baixar-app.php
 * Envie o APK assinado para: app/Improve-SST.apk (na mesma pasta que este PHP).
 * Nunca coloque certificados de assinatura nem dados de clientes dentro da pasta publica.
 */
declare(strict_types=1);
$apkPath = __DIR__ . '/app/Improve-SST.apk';
$available = is_file($apkPath) && is_readable($apkPath) && filesize($apkPath) > 100000;

if (isset($_GET['download'])) {
    if (!$available) {
        http_response_code(503);
        header('Content-Type: text/plain; charset=utf-8');
        exit('O APK ainda nao foi publicado. Entre em contato com a equipe Improve.');
    }
    header('Content-Type: application/vnd.android.package-archive');
    header('Content-Disposition: attachment; filename="Improve-SST.apk"');
    header('Content-Length: ' . filesize($apkPath));
    header('X-Content-Type-Options: nosniff');
    header('Cache-Control: no-store');
    readfile($apkPath);
    exit;
}
header('Content-Type: text/html; charset=utf-8');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');
?>
<!doctype html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="theme-color" content="#123b56">
<title>Baixar aplicativo | Improve SST Online</title>
<style>
*{box-sizing:border-box}
body{margin:0;min-height:100vh;display:grid;place-items:center;padding:20px;background:radial-gradient(circle at 6% 10%,#176c78 0,transparent 28%),linear-gradient(135deg,#09283e,#123b56 55%,#08767b);color:#15394f;font-family:Inter,system-ui,-apple-system,Segoe UI,Arial,sans-serif}
.card{width:min(490px,100%);background:#fff;border-radius:22px;box-shadow:0 20px 50px #061d3548;padding:clamp(24px,5vw,38px)}
.logo{width:100%;max-height:92px;object-fit:contain;object-position:left center;margin-bottom:14px}
.eyebrow{font-size:11px;color:#058a87;text-transform:uppercase;letter-spacing:1.3px;font-weight:800}
h1{font-size:28px;letter-spacing:-1px;line-height:1.13;margin:12px 0}
p{line-height:1.55;color:#597083;font-size:14px}
a.download{display:flex;align-items:center;justify-content:center;min-height:50px;background:linear-gradient(120deg,#088e8d,#066d7d);color:white;border-radius:11px;text-decoration:none;font-weight:750;margin:24px 0 8px}
.note{font-size:12px;background:#edf7f6;border-radius:10px;padding:13px 15px;color:#42627a}
.steps{font-size:13px;color:#36546c;line-height:1.7;padding-left:22px}
.small{font-size:12px}.disabled{display:block;background:#edf2f6;color:#50677b;border-radius:10px;padding:14px;margin:20px 0;text-align:center}
</style>
</head>
<body><main class="card">
<img src="app/logo-improve-oficial.png" alt="Improve Engenharia e Segurança do Trabalho" class="logo">
<span class="eyebrow">Aplicativo oficial para Android</span>
<h1>Improve SST Online no seu celular</h1>
<p>Acesse o controle de documentos, exames, treinamentos, EPIs, solicitações e visitas técnicas diretamente pelo aplicativo.</p>
<?php if ($available): ?>
<a class="download" href="?download=1">Baixar APK para Android</a>
<?php else: ?>
<div class="disabled">O instalador ainda não foi publicado.</div>
<?php endif; ?>
<ol class="steps"><li>Baixe o APK no seu Android.</li><li>Abra o arquivo baixado.</li><li>Se solicitado, autorize a instalação por esta fonte confiável.</li><li>Abra o aplicativo e entre com sua conta Improve.</li></ol>
<p class="note"><strong>Importante:</strong> baixe apenas do endereço oficial da Improve. O aplicativo necessita de internet; seus dados continuam no servidor seguro do portal.</p>
<p class="small"><a href="/">Voltar ao portal</a></p>
</main></body></html>
