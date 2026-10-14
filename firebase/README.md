# Vínculos e Chatbox no Realtime Database

O app usa o projeto do `app/google-services.json`. Não é necessário instalar o APK da pasta Chatbox.

## Estrutura

- `usuarios/pais/{uid}` e `usuarios/filhos/{uid}`: a chave é o UID do Firebase Authentication.
- `usuarios/filhos/{uid}/idResponsavel`: UID do responsável; pode estar ausente antes do vínculo.
- `chatbox/{uidDoFilho}/mensagens/{id}`: `id`, `role` (`user` ou `assistant`), `content` e `createdAt` (horário do servidor em milissegundos).

A tela recupera as últimas 100 mensagens; todas permanecem no banco. A API recebe até 40 mensagens recentes, além das instruções de sistema. A conversa é contínua por conta. A gravação da mensagem do jovem é confirmada antes da chamada à IA, e a resposta só é exibida depois de confirmada no Firebase. Se a conexão cair, o botão Tentar novamente reutiliza as mesmas chaves. Se o processo encerrar com uma pergunta já salva, ela é retomada ao abrir a tela.

## Regras

`database.rules.fragment.json` é um **fragmento para integrar às regras atuais**, não um substituto completo das regras do projeto. Não foi publicado no Firebase. Preserve as regras de cadastro, regras de uso, alertas e demais funcionalidades ao fazer a integração. Mescle os índices, sem apagar índices existentes.

As regras de leitura e gravação do banco ativo não estão versionadas neste repositório. Revise as permissões nos nós ancestrais antes de publicar: uma permissão ampla de leitura/gravação em `/`, `/usuarios` ou `/chatbox` também permite acesso aos descendentes e não é restringida por uma regra mais interna. As regras de criação de perfil devem permitir somente a criação da própria conta autenticada e impedir mudança de papel. Não conceda escrita irrestrita sobre perfis de outras contas.

O fragmento permite que cada jovem leia/escreva seu chat e que o responsável consulte filhos usando `orderByChild('idResponsavel').equalTo(auth.uid)`. A leitura de `nome` por ID permite verificar a conta de destino sem abrir o perfil inteiro. O vínculo é criado por uma das duas contas autenticadas e não troca um responsável já associado. Repetir o mesmo vínculo é permitido. Não foi adicionada uma tela de leitura do chat pelo responsável.

O cadastro existente ainda consulta duplicidade de e-mail, telefone e CPF. Essas consultas e a criação inicial do perfil precisam das regras já usadas pelo projeto; este fragmento não concede acesso público a perfis para viabilizá-las.

Referência: [regras do Realtime Database](https://firebase.google.com/docs/database/security) e [transações no Android](https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions).

## Configuração da IA

`OPENROUTER_API_KEY` e `OPENROUTER_MODEL` são lidos, nesta ordem, do ambiente, do `local.properties` deste projeto e do `../chatbox/local.properties`. Nenhuma chave é copiada para o código-fonte. Para desenvolvimento, a chave continua embutida no APK, assim como no Chatbox original; uma distribuição pública deve usar um backend autenticado para manter a credencial no servidor.

## Verificação manual com contas de teste

1. Criar um jovem deixando o ID do responsável vazio; abrir **Vincular responsável pelo ID** e copiar seu ID.
2. Na conta do responsável, abrir **Vincular contas pelo ID**, colar o ID do jovem e vincular. Conferir a atualização em **Meus filhos**, inclusive se a lista estiver aberta em outro dispositivo.
3. Com outro jovem sem vínculo, fazer o caminho inverso: informar o ID do responsável na conta do jovem. Repetir o vínculo para conferir a operação idempotente.
4. Testar próprio ID, ID inexistente, e-mail, espaços externos, conta de perfil errado e jovem já vinculado a outro responsável. Os erros devem aparecer sem alterar o vínculo existente.
5. Abrir Chatbox, aguardar a abertura, enviar uma mensagem fictícia, sair da tela e reabri-la. Conferir mensagens e timestamps em `chatbox/{uid}/mensagens`.
6. Testar interrupção da rede, erro da API, rotação e encerramento do processo após uma pergunta salva. A retomada não deve duplicar a pergunta.
7. No emulador ou simulador de regras do Firebase, conferir que outra conta não lê/escreve o chat, que um responsável não substitui outro e que a consulta de filhos sem filtro é recusada.

Contas antigas cuja chave seja e-mail, ou que tenham somente `emailResponsavel`, precisam ser migradas para UIDs com credenciais administrativas e backup, ou ter o vínculo refeito na tela quando já possuírem perfil sob UID. O app não faz migração automática baseada em e-mails, para não atribuir filhos à conta errada.

## Validação realizada nesta alteração

- `:app:assembleDebug`: passou; APK em `app/build/outputs/apk/debug/app-debug.apk`.
- `:app:testDebugUnitTest`: 12 testes passaram (8 do fluxo de chat, 3 de vínculo e 1 teste já existente).
- JSON das regras e XMLs de recursos: sintaxe verificada.
- `:app:lintDebug`: não concluído. O download de `com.android.tools.external.com-intellij:kotlin-compiler:31.7.3` ficou sem avançar, e o artefato não está disponível no cache para rodar offline.
- Sem validação de ponta a ponta com contas reais, sem publicação de regras e sem migração de dados no banco ativo.

Neste Windows, o Java do Android Studio apresentou falha ao conectar sockets Unix usados pelo Gradle. A compilação foi executada com `JAVA_HOME` apontando para o `jbr` do Android Studio e `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\Users\Workstation\AndroidStudioProjects\Supervisionador\gradlew.bat`. Apontar essa propriedade para um arquivo existente força o fallback de sockets Unix para TCP local. Esse ajuste foi usado somente no processo de compilação, sem alterar as configurações globais do sistema.
