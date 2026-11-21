# Plano de verificação antes de enviar APK a clientes

Use um celular Android real (de preferência um com Android 10 e outro com Android 14/15+). Testes no servidor de produção devem usar conta de teste e registros que possam ser excluídos de forma segura.

- [ ] O aplicativo abre `https://sistema.improveengenhariasst.com.br/` em HTTPS.
- [ ] Formulário de login funciona e a sessão não se perde ao navegar.
- [ ] Admin vê apenas as opções permitidas; usuário cliente não vê ações administrativas.
- [ ] O menu mobile abre, fecha e mantém o layout correto.
- [ ] Troca de empresa funciona para administradores e não vaza dados para clientes.
- [ ] Dashboard, Documentações, OSS, EPI, Exames, Riscos, Treinamentos, Certificados, Bombeiros, Compressores, Visitas, Pendências e Solicitações carregam.
- [ ] Seleção de PDF/imagem funciona em evidências de correção e solicitações.
- [ ] Upload de arquivos em ficha de EPI e documentos funciona.
- [ ] Arquivos PDF de laudos, certificados e anexos baixam e abrem corretamente.
- [ ] PDFs gerados em links `target=_blank` funcionam (podem exigir ajuste específico do site).
- [ ] Links de WhatsApp e e-mail abrem os aplicativos externos.
- [ ] E-mail de chamado continua sendo enviado pelo servidor; o APK não recebe credenciais SMTP.
- [ ] Ao cair a internet, aparece tela de erro e o botão de tentar novamente funciona.
- [ ] Ao pressionar Voltar, a WebView retorna à página anterior.
- [ ] Depois de fazer logout, páginas protegidas não ficam disponíveis sem login.
- [ ] Instalação via `baixar-app.php` e atualização assinada com a mesma chave funcionam.

## Observações

- O app não garante leitura offline de arquivos: o servidor PHP/MySQL continua como fonte única dos dados.
- A WebView pode precisar de ajustes adicionais para PDFs renderizados inline, downloads `blob:` e PDFs abertos por `window.open`; valide os fluxos reais do sistema antes da distribuição ampla.
- O upload permite o seletor de arquivos do Android; captura direta da câmera não está implementada nesta primeira versão.
- A política de acesso por empresa deve continuar aplicada exclusivamente pelo servidor, não apenas escondida no app.
