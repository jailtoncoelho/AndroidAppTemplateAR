# Fase 2 — Sanrio AR

## Modelos 3D
A tela de seleção lê automaticamente os arquivos `.glb` presentes em `app/src/main/assets`. Para a versão temática Sanrio, adicione:
- `hello_kitty.glb` (ou `hello_kitty (1).glb`)
- `my_melody.glb`
- `kuromi.glb`
- `cinamon.glb` (Cinnamoroll)
- `pompompurin.glb`

O seletor normaliza nomes com sufixos como ` (1)` para exibir o nome correto do personagem.

## Interação
1. Escolher um personagem na lista.
2. Abrir a câmera e posicionar o modelo no ambiente.
3. Pressionar **Coletar**: o modelo é removido da cena, a pontuação aumenta em um ponto e toca um efeito de confirmação.
4. Voltar à lista para selecionar outro personagem.

## Áudio
A coleta toca um efeito sonoro curto. A tela da câmera também gera uma trilha ambiente suave localmente, sem depender de arquivo de áudio externo. A trilha é interrompida quando a tela sai de primeiro plano e liberada ao fechar a câmera.

## Correções desta revisão
- O modelo inicial agora procura primeiro Hello Kitty e, se não estiver disponível, seleciona outro GLB encontrado nos assets.
- A câmera não tenta carregar silenciosamente um modelo antigo chamado `neutrofilo.glb`.
- Se o arquivo selecionado não existir, o app informa o problema em vez de prosseguir com o carregamento.

## Validação necessária
A compilação deve ser executada no Android Studio. O teste de RA precisa ser feito em um celular compatível com Google ARCore, verificando carregamento, posicionamento, coleta, pontuação e áudio. Prints dos modelos renderizados em RA devem ser registrados no dispositivo.
