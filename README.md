# Caçadores de Mito — AndroidAppTemplateAR

Atividade de Desenvolvimento de Aplicativos: **Jogo em Realidade Aumentada – Game Over**,
Jailton Coelho, 2026 (PDF de 18 páginas, Aula 2).

Branch da atividade: [`03-11.2`](https://github.com/henryportes880/AndroidAppTemplateAR/tree/03-11.2).

## Regra implementada

O jogador começa com três vidas. A **espada** é o objeto perigoso: tocar no seu
modelo 3D retira uma vida, respeitando um intervalo mínimo de um segundo entre
danos. A detecção usa o hit test e o evento de toque do próprio nó do SceneView;
tocar no fundo ou nos outros modelos não retira vidas. Em AR, é necessário
posicionar o modelo em uma superfície primeiro.

A regra aparece em um diálogo antes de iniciar e em um resumo junto às vidas.
Com zero vidas, a partida termina, a cena e suas tarefas são encerradas, e
`GameOverActivity` informa **“Suas vidas acabaram.”**. A Activity da partida é
finalizada para impedir que Voltar a recupere. **Jogar novamente** cria uma nova
`MainActivity`, com três vidas, regra inicial e a espada reposta na cena.

Os modelos anteriores, a seleção na outra aba, o posicionamento, o reposicionamento
e os modos com câmera e 3D foram preservados. O modelo inicial agora é a espada
para que a condição de derrota esteja acessível ao iniciar.

## Revisão antes das alterações

O repositório foi clonado neste ambiente; não havia checkout prévio com alterações
locais acessíveis. Após a confirmação da branch, a revisão e a implementação usaram
`03-11.2`, originalmente no commit `99709e1`. O estado inicial estava limpo. Não
foram encontrados arquivos `AGENTS.md`; o README anterior continha somente o nome
do repositório. A integração `feature-sceneview` já estava presente nessa branch.
Alterações eventualmente existentes apenas no computador do aluno não puderam ser
inspecionadas neste ambiente.

| Requisito do PDF | Classificação inicial no código | Resultado da implementação |
| --- | --- | --- |
| Usar a versão integrada com feature-sceneview (p. 6) | Concluído no código; não testado em Android | Preservada a integração SceneView 2.2.1 |
| Definir ao menos uma condição de derrota (p. 2–4, 18) | Ainda não implementado | Três contatos com a espada encerram a partida |
| Explicar a regra antes da partida (p. 4) | Ainda não implementado | Diálogo antes de iniciar e resumo na interface |
| Estado da partida e vidas na interface (p. 7) | Ainda não implementado | GameViewModel e contador de vidas |
| Dano com intervalo de 1 s, se usada a regra das vidas (p. 5, 8) | Ainda não implementado | Relógio monotônico e bloqueio de danos repetidos |
| Ligar a derrota a uma interação real (p. 9) | Ainda não implementado | Toque no nó 3D da espada |
| Encerrar e bloquear ações, tarefas e sons (p. 10–11) | Ainda não implementado | Bloqueios, cancelamento de jobs, destruição da cena e liberação da câmera; não havia sons de partida |
| GameOverActivity com título, motivo e botão (p. 12–14) | Ainda não implementado | Activity no mesmo pacote de CameraFragment, interface criada por código |
| Registrar Activity não exportada (p. 15) | Ainda não implementado | Registro no AndroidManifest.xml |
| Reiniciar estado e repor objetos (p. 16) | Ainda não implementado | Activity e ViewModels novos; modelo inicial recarregado; não havia pontuação, cronômetro ou coleta |
| Rotação não recuperar vidas (p. 16) | Ainda não implementado | ViewModel no escopo da Activity conserva vidas e intervalo entre danos, inclusive ao trocar de aba |
| Tela única e Voltar não retomar partida (p. 17) | Ainda não implementado | Proteção contra abertura repetida e finish da Activity anterior |
| Testar o fluxo completo no celular (p. 17–18) | Pendente | Exige o roteiro abaixo em dispositivo físico |
| Entregar branch, vídeo e descrição (p. 18) | Incompleto: branch existia, funcionalidade ausente | Descrição neste README; vídeo e postagem no Classroom dependem do aluno |

As três vidas são o exemplo do professor; o requisito geral permite outra condição
de derrota coerente com o jogo. Aqui foi escolhido seguir o exemplo com interação
por toque, explicitamente permitida na página 9. Não foram acrescentados pontuação,
coleta, cronômetro, Firebase ou outras regras que a atividade não exige.

## Validação

A análise de código não equivale a um teste funcionando no celular.

- `assembleDebug`: passou, APK debug gerado em compilação limpa com JDK 17.
- `testDebugUnitTest`: passou, 6 testes no total (5 novos e 1 preexistente), sem falhas.
- `lintDebug`: passou, 0 erros e 54 avisos nas partes preexistentes (textos, recursos,
  catálogo e versões de dependências); não foram ocultados ou desativados.
- `assembleDebugAndroidTest`: passou; APK de testes Android gerado. Execução
  desses testes em Android permanece pendente.
- Testes Android (`GameFlowTest`): preparados, mas não executados sem dispositivo.
- Toque no modelo, AR/câmera, aparência, rotação, botão Voltar e reinício reais:
  ainda precisam de teste em Android. O ADB não encontrou dispositivos conectados;
  não há emulador instalado nem `/dev/kvm` disponível neste ambiente.

Os testes unitários novos cobrem estado inicial, intervalo de 1 s (inclusive o
primeiro dano no tempo zero), derrota no terceiro dano, bloqueio após a derrota,
reentrada sem recuperar vidas e estado novo de uma nova partida. Os testes Android
cobrem retenção do estado durante recriação da Activity e mensagem/reinício da tela
final; não substituem o teste do toque e do rastreamento físico.

## Executar

1. Faça checkout da branch `03-11.2` e abra o projeto no Android Studio.
2. Use JDK 17, Android SDK 36 e Build Tools 35.0.0. Sincronize o Gradle.
3. Conecte um celular com Android 7.0/API 24 ou superior, com depuração USB ativada,
   selecione o módulo `app` e execute **Run**.
4. Leia a regra e toque em **Iniciar partida**. Autorize a câmera.
   Em celular compatível com ARCore e Google Play Services for AR instalado,
   aponte para uma mesa ou chão e toque em **Posicionar**. O projeto preserva o
   fallback de câmera/3D para ambientes sem ARCore ou sem permissão de câmera.

Pelo terminal (JDK e SDK configurados):

```sh
bash gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
bash gradlew installDebug
# Com um dispositivo/emulador conectado, para os testes Android:
bash gradlew connectedDebugAndroidTest
```

APK gerado: `app/build/outputs/apk/debug/app-debug.apk` (não versionado).
Não é necessário configurar Firebase para este fluxo de Game Over.

## Roteiro curto de teste físico e vídeo

1. Inicie: confira a regra antes da partida e **3 vidas** na interface.
2. Posicione a espada em AR (ou use a visualização disponível). Toque no fundo e
   confirme que não há dano. Toque na espada: devem restar **2 vidas**.
3. Repita um toque em menos de 1 s: as vidas devem continuar em 2. Espere pelo
   menos 1 s após o dano anterior e toque: deve restar **1 vida**.
4. Gire o aparelho e alterne entre as abas: a vida restante deve continuar em 1.
   Se a cena for recriada, posicione a espada novamente. Exiba o escudo: tocar nele
   não deve retirar vida. Volte à espada.
5. Após o intervalo, toque na espada: deve abrir uma única tela **GAME OVER** com
   **Suas vidas acabaram.** e **Jogar novamente**. A cena/câmera anterior deve parar.
6. Toque em **Jogar novamente**: deve aparecer uma nova partida com regra e 3 vidas.
   Provoque outra derrota e pressione **Voltar**: a partida encerrada não deve voltar.

Grave um vídeo curto mostrando a redução das vidas, a derrota, o motivo e o
reinício com três vidas. Publique no Classroom o link desta branch, o vídeo e,
como descrição, o parágrafo da seção **Regra implementada**. Teste em celular
compatível com ARCore para demonstrar realidade aumentada física; um teste em
modo 3D não comprova o rastreamento AR.
