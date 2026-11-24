# Fase 4 — Game Over (Sanrio AR)

## Regra de derrota
A partida começa com 3 vidas. Cada contato com o obstáculo retira 1 vida. Há um intervalo mínimo de 1 segundo entre danos. Ao chegar a zero, a partida termina e a tela informa: "Suas vidas acabaram."

## Como testar a regra nesta versão
O aplicativo Sanrio AR atual é um visualizador/coletor de personagens e ainda não possui obstáculos físicos nem detecção automática de colisão na cena AR. Para tornar a condição verificável sem inventar uma colisão que não existe, a tela inclui o botão **Atingir obstáculo (-1 vida)**. Cada toque válido simula um contato com obstáculo e aplica a mesma função/regra de dano. Toques em menos de 1 segundo são ignorados.

## Comportamento após derrota
- O estado de partida é marcado como encerrado.
- Os botões de posicionamento, coleta e obstáculo são desativados.
- A cena AR é ocultada e o modelo é removido.
- A trilha ambiente e o tom de áudio são interrompidos.
- A Activity anterior é encerrada e a tela Game Over mostra o motivo.
- "Jogar novamente" abre uma MainActivity nova, reiniciando vidas e pontuação.

## Estado durante rotação
As vidas, a flag de encerramento e o horário do último dano ficam no SharedModelViewModel, preservado durante a recriação da tela. Uma nova partida cria um novo escopo de Activity/ViewModel.

## Roteiro de teste manual exigido
1. Iniciar: conferir 3 vidas.
2. Tocar em "Atingir obstáculo": conferir 2 vidas.
3. Tentar novamente antes de 1 segundo: a vida não deve diminuir.
4. Após 1 segundo, tocar de novo: conferir 1 vida.
5. Após mais 1 segundo, tocar novamente: abrir Game Over com o motivo.
6. Tentar coletar/posicionar após a derrota: nenhuma ação deve ocorrer.
7. Conferir que o áudio ambiente parou.
8. Tocar em "Jogar novamente": conferir nova partida com 3 vidas e pontuação zerada.

**Limitação conhecida:** a interação com obstáculo é simulada pelo botão de teste. A detecção automática de colisão com um objeto perigoso na realidade aumentada ainda precisa ser adicionada se o jogo passar a ter obstáculos reais.
