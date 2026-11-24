# Sanrio AR — personagens e configuração

## Arquivos 3D esperados
Coloque os arquivos GLB diretamente em `app/src/main/assets/`:

| Arquivo | Nome exibido |
|---|---|
| `hello_kitty.glb` (ou `hello_kitty (1).glb`) | Hello Kitty |
| `my_melody.glb` | My Melody |
| `kuromi.glb` | Kuromi |
| `cinamon.glb` | Cinnamoroll |
| `pompompurin.glb` | Pompompurin |

A tela de seleção lista automaticamente os arquivos GLB encontrados. O nome `hello_kitty (1).glb` também é reconhecido e exibido como Hello Kitty.

## Funcionamento
1. Abra a tela de seleção de personagens.
2. Toque em **Ver na câmera**.
3. Aguarde o carregamento do modelo e posicione-o no ambiente.
4. Toque em **Coletar** para remover o personagem da cena, somar um ponto e ouvir o efeito de confirmação.
5. A câmera toca uma trilha ambiente suave enquanto a tela está ativa.

## Modelo inicial
Ao abrir a câmera sem uma escolha anterior, o aplicativo procura primeiro Hello Kitty. Se esse arquivo não existir, escolhe o primeiro GLB disponível. Se o arquivo selecionado estiver ausente, apresenta uma mensagem em vez de tentar carregar um caminho inválido.

## Atenção — arquivos binários
Os arquivos GLB são binários e precisam ser adicionados ao GitHub como arquivos do projeto, não apenas existir na cópia local do Android Studio. Confira na página da branch se os cinco arquivos aparecem em `app/src/main/assets/`. Nesta revisão, o conector do GitHub não localizou esses cinco binários na branch; portanto, não marque a inclusão dos modelos como concluída até confirmar o envio.

## Testes de entrega
- Compile com **Build > Compile 'AndroidAppTemplateAR.app.main'**.
- Execute em um celular compatível com Google ARCore.
- Teste os cinco modelos, posicionamento, coleta, pontuação e áudio.
- Registre fotos/prints dos personagens renderizados em RA.
