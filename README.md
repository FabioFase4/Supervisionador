# Projeto TCC
Supervisõa parental nas Redes Sociais

# Objetivo
Construir um Aplicativo Android que supervisione os jovens nas redes sociais

# Banco de Dados
Firebase -> NoSQL

# Base
Em Mudança


## Chatbox e vínculo por ID

O jovem pode abrir **Conversar com o Chatbox** na tela inicial. O histórico é salvo no Firebase e recuperado ao reabrir. A integração reaproveita a interface e a política de conversa da pasta `chatbox`.

Ambos os perfis têm **Vincular contas pelo ID**, com o próprio UID para copiar e um campo para informar o UID da outra pessoa. O ID no cadastro do jovem é opcional. A lista de filhos consulta `idResponsavel` pelo UID autenticado e acompanha atualizações em tempo real, sem depender de um e-mail recebido por Intent.

Configuração, estrutura do banco, fragmento de regras e roteiro de testes: [firebase/README.md](firebase/README.md).

Validação local:

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```
