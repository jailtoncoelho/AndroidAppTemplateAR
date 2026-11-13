# Caçadores de Mito

Jogo Android de reflexo em realidade aumentada, construído sobre o
AndroidAppTemplateAR e a integração **feature-sceneview / SceneView 2.2.1**.

Branch da entrega de 10/11: [`10/11`](https://github.com/henryportes880/AndroidAppTemplateAR/tree/10/11).

## Como jogar

1. Abra a aba **Jogar**. Em AR, aponte para uma mesa ou chão e toque em
   **Posicionar**. O aplicativo também preserva os modos de câmera/3D da base.
2. Toque em **Iniciar caça**, leia as regras e confirme **Iniciar partida**.
3. **Recupere 5 escudos em 30 segundos.** Um objeto aparece por vez, alternando
   entre escudo e espada a cada 2 segundos de jogo.
4. Toque no **escudo** para coletar. Evite a **espada**: tocá-la tira uma das suas
   três vidas. Cada aparição só conta uma vez; o objeto desaparece após o toque e
   o próximo aparece na troca programada. Tocar no fundo não produz efeito.
5. Ao coletar 5 escudos, você vence. Com zero vidas, ou tempo esgotado antes da
   meta, aparece **Game Over**, informando o motivo e o resultado da coleta.
6. **Jogar novamente** inicia uma partida nova: 3 vidas, 0 escudos, 30 segundos,
   escudo inicial e regras antes de começar.

O relógio só corre com a tela em primeiro plano, modelo carregado e cena pronta;
em AR, o objeto deve estar ancorado e a câmera rastreando. Trocar de aba, girar o
aparelho, minimizar ou reposicionar pausa o tempo durante a transição, sem repor
vidas, escudos ou tempo. É preciso reposicionar após recriar a cena AR.

A aba **Modelos** mantém os arquivos anteriores para exploração antes da partida.
Durante uma caça, ela não permite substituir o alvo e alterar a regra de coleta.
Os objetos do jogo mantêm a mesma âncora AR entre as rodadas. Escudo e espada são
reutilizados na mesma cena, evitando carregar os GLBs a cada aparição. Os gestos
de edição ficam disponíveis na exploração e são desativados nos alvos da caça.

## Atividade do professor e revisão inicial

Referência: **Desenvolvimento de Aplicativos — Jogo em Realidade Aumentada –
Game Over**, Jailton Coelho, 2026, Aula 2 (18 páginas).

O repositório foi clonado neste ambiente. Não havia alterações locais acessíveis.
A branch confirmada pelo aluno foi `03-11.2`, originalmente em `99709e1`.
Não foram encontrados arquivos `AGENTS.md`; o README original continha apenas
o nome do repositório. Arquivos eventualmente existentes apenas no computador do
aluno não puderam ser inspecionados. A integração da cena já estava presente;
as regras e a tela final estavam ausentes. Os modelos e a base de visualização
foram preservados. A atualização do jogo acrescenta uma meta de coleta e um
prazo, dando propósito ao uso dos escudos e espadas existentes.

| Requisito do PDF | Situação inicial em `99709e1` | Implementação atual |
| --- | --- | --- |
| Usar a integração feature-sceneview (p. 6) | Concluído no código | Preservado SceneView 2.2.1 e fallback de câmera/3D |
| Definir e explicar a derrota (p. 2–4) | Ainda não implementado | Zero vidas ou prazo de 30 s sem coletar 5 escudos; regras antes do início |
| Mostrar vidas e controlar o estado (p. 7) | Ainda não implementado | HUD com vidas, escudos e tempo; GameViewModel |
| Intervalo de 1 s entre danos, se usada a regra das vidas (p. 5, 8) | Ainda não implementado | Intervalo mantido, além de permitir um dano por aparição da espada |
| Interação real ligada à derrota (p. 9) | Ainda não implementado | Hit test/toque no nó 3D; espada tira vida, escudo pontua |
| Encerrar ações e tarefas (p. 10–11) | Ainda não implementado | Bloqueia ações e cancela relógio, carregamentos e watchdogs; libera cena e câmera; não há sons de partida |
| GameOverActivity com motivo e botão (p. 12–14) | Ainda não implementado | Mesma classe/pacote; interface por código; derrota e vitória mostram coleta e reinício |
| Activity não exportada no Manifest (p. 15) | Ainda não implementado | Registrada com exported=false |
| Reiniciar todo o estado e repor objetos (p. 16) | Ainda não implementado | Nova MainActivity, novos ViewModels e cena nova |
| Girar não recuperar vidas (p. 16) | Ainda não implementado | Estado no escopo da Activity: mantém vidas, coleta, tempo e rodada |
| Tela única e Voltar não recuperar partida (p. 17) | Ainda não implementado | Guarda contra abertura repetida e finalização da Activity anterior |
| Testar no celular (p. 17–18) | Pendente | Ainda requer o roteiro físico abaixo |
| Entregar branch, vídeo e regra (p. 18) | Incompleto: branch existia | Regra e branch documentadas; gravação e postagem no Classroom pendentes |

O PDF permite escolher uma condição coerente com o jogo. Três vidas são um
exemplo, e o toque em objeto perigoso é permitido na página 9. Não foi necessário
configurar Firebase, obter novos modelos ou trocar as tecnologias do projeto.

## Validação

Validação final executada com JDK 17 e SDK 36:

- `assembleDebug`: passou; APK gerado.
- `testDebugUnitTest`: passou, 13 testes no total, sem falhas (12 da lógica do
  jogo e 1 preexistente). Cobrem vitória, as duas derrotas, coleta única, toques
  atrasados, alternância de objetos, prazo e bloqueio após o encerramento.
- `lintDebug`: passou, 0 erros e 53 avisos nas partes preexistentes do projeto
  (recursos, textos e dependências). Nenhum aviso foi ocultado.
- `assembleDebugAndroidTest`: passou. Isso comprova a compilação dos testes
  Android, não a execução deles em um dispositivo.

Análise de código, testes unitários e compilação não comprovam que o AR funciona
em um celular. Não há dispositivo conectado ao ADB, emulador instalado ou
`/dev/kvm` disponível neste ambiente. Os testes Android foram preparados para
recriação da Activity, mensagem final, reinício e vitória, mas precisam de um
Android para serem executados.

## Executar

Abra esta branch no Android Studio, use **JDK 17**, **SDK 36**, Build Tools 35.0.0
e execute o módulo `app` em Android 7.0/API 24 ou superior.
Para AR físico, use celular compatível com ARCore e Google Play Services for AR
instalado; autorize a câmera. Os modos de câmera/3D são alternativas de
visualização e não comprovam o rastreamento AR.

```sh
bash gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
# Com Android conectado e depuração USB ativada:
bash gradlew installDebug
bash gradlew connectedDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk` (não versionado).
O APK Android não pode ser instalado no iPhone.

## Roteiro de teste e vídeo real

- **Preparação:** abrir, posicionar, iniciar e conferir 3 vidas, 0/5 escudos e 30 s.
  O prazo não deve diminuir durante carregamento ou posicionamento.
- **Coleta/vitória:** tocar somente nos escudos de aparições diferentes. Tocar
  repetidamente no mesmo objeto não deve duplicar a coleta. No quinto, conferir
  Vitória e 5/5; reiniciar e conferir o estado inicial.
- **Derrota por vidas:** iniciar outra partida e tocar nas três primeiras
  espadas que aparecerem. Conferir 2, 1 e 0 vidas, Game Over e o motivo.
- **Derrota por tempo:** reiniciar e não coletar escudos; após 30 segundos de
  jogo ativo, conferir Game Over por tempo, mesmo ainda tendo vidas.
- **Estado/câmera:** minimizar, girar, alternar abas e reposicionar. Conferir
  pausa nas transições, sem recuperar recursos. Em AR, conferir que a troca de
  escudo para espada mantém a posição ancorada.
- **Encerramento:** verificar que câmera e objetos param, só há uma tela final
  e Voltar não reabre a partida encerrada. Repetir o reinício.

Grave a execução real de uma derrota e do reinício para o Classroom. Uma
simulação ilustrativa não substitui esse teste ou a gravação do aplicativo.

Descrição pronta para entregar:

> O objetivo é recuperar 5 escudos em 30 segundos. Escudos e espadas alternam
> automaticamente a cada 2 segundos. Tocar no escudo aumenta a coleta; tocar na
> espada retira uma das 3 vidas. Cada aparição só conta uma vez. A partida termina
> quando as vidas acabam ou o tempo esgota antes da meta. O Game Over informa o
> motivo, interrompe as ações e permite reiniciar com o estado inicial. Ao atingir
> a meta, a tela final mostra vitória.
