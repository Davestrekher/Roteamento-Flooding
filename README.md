# Roteamento-Flooding
Simulação do algortimo de roteamento por inundação (floooding). Quatro variações são propostas e explicadas, com a exibição de um contador de pacotes gerados até que o pacote chegue ao destino durante a simulação.

## Requisitos
Certifique que o JavaFX está instalado

## Arquivo de configurações (backbone.txt)
- Este arquivo determina o grafo utilizado para a simulação.
- A formatação do arquivo se dá desta forma:
- 1° linha: quantidade_de_nós; (max 26)
- Demais linhas: NÓ1;NÓ2;PESO_DA_ARESTA

## Compilar
javac Principal.java

## Executar
java Principal
