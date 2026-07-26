/* ***************************************************************
* Autor............: Davi Gabrielli Santos
* Matricula........: 202410855
* Inicio...........: 16/03/2026
* Ultima alteracao.: 27/03/2026
* Nome.............: Roteador
* Funcao...........: Classe que armazena as informaoces importantes
                     representar os roteadores da sub-rede representados
                     na tela.
*************************************************************** */

package modelo;

import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class Roteador{
  //Representam e identificam cada roteador individual na tela
  private ImageView imagem;
  private String identificador;
  private LinkedList<Aresta<Roteador>> vizinhos;
  private Pane raiz;
  private Semaphore mutex;
  private Map<String, Integer> mapa = new HashMap<>();

  private int opcaoRoteamento;
  private int idSimulacao;

  //Variavel global que conta quantos pacotes foram gerados
  private IntegerProperty contador;
  //Variavel global que define se o pacote ja chegou ao seu destino para interromper o contador de pacotes gerados
  private static boolean pacoteChegou = false;

  /* ***************************************************************
	* Metodo: Roteador
	* Funcao: Construtor da classe
	* Parametros: imagem = representacao do roteador na tela, identificador =
                numero que identifica cada um dos roteadores.
	* Retorno: Objeto da classe Roteador
	*************************************************************** */
  public Roteador(ImageView imagem, String identificador, int opcaoRoteamento, Pane raiz, Semaphore mutex, IntegerProperty contador){
    this.imagem = imagem;
    this.identificador = identificador;
    this.opcaoRoteamento = opcaoRoteamento;
    this.raiz = raiz;
    this.mutex = mutex;
    this.contador = contador;
  }//Fim do metodo Roteador

  /* ***************************************************************
	* Metodo: getIdentificador
	* Funcao: Retorna o identificador deste identificador.
	* Parametros: Nenhum
	* Retorno: String
	*************************************************************** */
  public String getIdentificador(){
    return this.identificador;
  }//Fim do metodo getIdentificador

  /* ***************************************************************
	* Metodo: setRoteamento
	* Funcao: Define qual algoritmo de roteamento sera usado
	* Parametros: opcaoRoteamento = indice do radioItemMenu (1 a 4)
	* Retorno: void
	*************************************************************** */
  public void setRoteamento(int opcaoRoteamento){
    this.opcaoRoteamento = opcaoRoteamento;
  }//Fim do metodo setRoteamento

  /* ***************************************************************
	* Metodo: setIdSimulacao
	* Funcao: Define o id da simulacao atual
	* Parametros: idSimulacao = id da simulacao atual
	* Retorno: void
	*************************************************************** */
  public void setIdSimulacao(int idSimulacao){
    this.idSimulacao = idSimulacao;
  }//Fim do metodo setIdSimulacao

  /* ***************************************************************
	* Metodo: getLayoutX
	* Funcao: Retorna a coordenada X da posicao do roteador.
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
  public double getLayoutX(){
    return imagem.getLayoutX() + (imagem.getFitWidth() / 2);
  }//Fim do metodo getLayoutX

  /* ***************************************************************
	* Metodo: getLayoutY
	* Funcao: Retorna a coordenada X da posicao do roteador.
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
  public double getLayoutY(){
    return imagem.getLayoutY() + (imagem.getFitHeight() / 2);
  }//Fim do metodo getLayoutY

  /* ***************************************************************
	* Metodo: addVizinhos
	* Funcao: Define a lista que contem todos roteadores adjacentes a
            este roteador
	* Parametros: vizinhos = lista de adjacencias
	* Retorno: void
	*************************************************************** */
  public void addVizinhos(LinkedList<Aresta<Roteador>> vizinhos){
    this.vizinhos = vizinhos;
  }//Fim do metodo addVizinhos

  /* ***************************************************************
	* Metodo: roteamento
	* Funcao: Direciona o pacote para o algoritmo de roteamento escolhido
	* Parametros: pacote = pacote com informacao de origem, destino e ttl,
                origemAtual = id do roteador de onde o pacote veio.
	* Retorno: void
	*************************************************************** */
  public void roteamento(Pacote pacote, String origemAtual) throws InterruptedException{
    switch(this.opcaoRoteamento){
      case 1:
        floodingOpcao1(pacote, origemAtual);
        break;
      case 2:
        floodingOpcao2(pacote, origemAtual);
        break;
      case 3:
        floodingOpcao3(pacote, origemAtual);
        break;
      case 4:
        floodingOpcao4(pacote, origemAtual);
        break;
      default:
        floodingOpcao1(pacote, origemAtual);
    }
  }//Fim do metodo roteamento

  /* ***************************************************************
	* Metodo: floodingOpcao1
	* Funcao: Simplesmente envia o pacote para todas os roteadores adjacentes
	          a este roteador
	* Parametros: pacote = pacote com informacao de origem, destino e ttl,
                origemAtual = id do roteador de onde o pacote veio.
	* Retorno: void
	*************************************************************** */
  public void floodingOpcao1(Pacote pacote, String origemAtual) throws InterruptedException{
    //Para as imagens de pacotes anteriores nao ficarem paradas na tela, esse metodo eh utilizado.
    //Note que isso nao altera o travamento causado por muitos pacotes gerados, porque o pacote
    //vai ser copiado e enviado de qualquer maneira, de forma que a informacao nao eh perdida.
    Platform.runLater(() -> {
      ImageView imagemAnterior = pacote.getImagem();
      imagemAnterior.setOpacity(0);
    });

    if (pacote.getDestino().equals(this.identificador)){
      mutex.acquire();
      pacoteChegou = true;
      mutex.release();
    }

    //Para cada roteador vizinho
    for(Aresta<Roteador> aresta : vizinhos){
      ImageView imagem = new ImageView(new Image("/assets/pomba00.png"));
      imagem.setPreserveRatio(true);
      imagem.setSmooth(true);

      imagem.setFitWidth(25);
      imagem.setFitHeight(25);

      Pacote novoPacote = new Pacote(
          pacote.getOrigem(),
          pacote.getDestino(),
          imagem,
          pacote.getTtl(),
          pacote.getNumSeq(),
          pacote.getIdSimulacao()
      );

      Platform.runLater(() -> {
        imagem.setLayoutX(this.getLayoutX());
        imagem.setLayoutY(this.getLayoutY());

        raiz.getChildren().add(imagem);

        new Thread(() -> {
          //Envia o pacote
          try{
            mutex.acquire();
            if (!pacoteChegou){
               Platform.runLater(() -> {
                contador.set(contador.get() + 1);//Atualiza o contador de pacotes gerados
              });
            }
            double destinoX = aresta.getDestino().getLayoutX();
            double destinoY = aresta.getDestino().getLayoutY();
            mutex.release();

            SimulacaoContexto ctx = new SimulacaoContexto();

            novoPacote.movimento(destinoX, destinoY);

            if (ctx.getIdSimulacao() != this.idSimulacao){
              return;
            }

            //Quando o pacote chegar, o roteamento se inicia no novo roteador
            aresta.getDestino().roteamento(novoPacote, this.getIdentificador());
          }catch(InterruptedException e){
              e.printStackTrace();
          }
        }).start();
      });

    }
  }//Fim do metodo floodingOpcao1

  /* ***************************************************************
	* Metodo: floodingOpcao2
	* Funcao: Simplesmente envia o pacote para todas os roteadores adjacentes
	          a este roteador, a excecao do roteador de onde este pacote
	          veio.
	* Parametros: pacote = pacote com informacao de origem, destino e ttl,
                origemAtual = id do roteador de onde o pacote veio.
	* Retorno: void
	*************************************************************** */
  public void floodingOpcao2(Pacote pacote, String origemAtual) throws InterruptedException{
    //Para as imagens de pacotes anteriores nao ficarem paradas na tela, esse metodo eh utilizado.
    //Note que isso nao altera o travamento causado por muitos pacotes gerados, porque o pacote
    //vai ser copiado e enviado de qualquer maneira, de forma que a informacao nao eh perdida.
    Platform.runLater(() -> {
      ImageView imagemAnterior = pacote.getImagem();
      imagemAnterior.setOpacity(0);
    });

    if (pacote.getDestino().equals(this.identificador)){
      mutex.acquire();
      pacoteChegou = true;
      mutex.release();
      return;
    }

    for(Aresta<Roteador> aresta : vizinhos){
      //Ignora o roteador que enviou o pacote
      if (aresta.getDestino().getIdentificador().equals(origemAtual)){
        continue;
      }

      ImageView imagem = new ImageView(new Image("/assets/pomba00.png"));
      imagem.setPreserveRatio(true);
      imagem.setSmooth(true);

      imagem.setFitWidth(25);
      imagem.setFitHeight(25);

      Pacote novoPacote = new Pacote(
          pacote.getOrigem(),
          pacote.getDestino(),
          imagem,
          pacote.getTtl(),
          pacote.getNumSeq(),
          pacote.getIdSimulacao()
      );
      Platform.runLater(() -> {
        imagem.setLayoutX(this.getLayoutX());
        imagem.setLayoutY(this.getLayoutY());

        raiz.getChildren().add(imagem);

        new Thread(() -> {
          try{
            //Envia o pacote
            mutex.acquire();
            if (!pacoteChegou){
               Platform.runLater(() -> {
                contador.set(contador.get() + 1);//Atualiza o contador de pacotes gerados
              });
            }
            double destinoX = aresta.getDestino().getLayoutX();
            double destinoY = aresta.getDestino().getLayoutY();
            mutex.release();

            novoPacote.movimento(destinoX, destinoY);

            //Mata a thread caso a simulacaos seja reiniciada
            SimulacaoContexto ctx = new SimulacaoContexto();
            if (ctx.getIdSimulacao() != this.idSimulacao){
              return;
            }

            //Quando o pacote chegar, o roteamento se inicia no novo roteador
            aresta.getDestino().roteamento(novoPacote, this.getIdentificador());
          }catch(InterruptedException e){
              e.printStackTrace();
          }
        }).start();
      });
    }
  }//Fim do metodo floodingOpcao2

  /* ***************************************************************
	* Metodo: floodingOpcao3
	* Funcao: Simplesmente envia o pacote para todas os roteadores adjacentes
	          a este roteador, a excecao do roteador de onde este pacote
	          veio. O ttl do pacote tambem subtraido em 1 e eh checado.
	          Se o valor do ttl, apos a subtracao, for igual a zero, o pacote
	          eh destruido.
	* Parametros: pacote = pacote com informacao de origem, destino e ttl,
                origemAtual = id do roteador de onde o pacote veio.
	* Retorno: void
	*************************************************************** */
  public void floodingOpcao3(Pacote pacote, String origemAtual) throws InterruptedException{
    //Para as imagens de pacotes anteriores nao ficarem paradas na tela, esse metodo eh utilizado.
    //Note que isso nao altera o travamento causado por muitos pacotes gerados, porque o pacote
    //vai ser copiado e enviado de qualquer maneira, de forma que a informacao nao eh perdida.
    Platform.runLater(() -> {
      ImageView imagemAnterior = pacote.getImagem();
      imagemAnterior.setOpacity(0);
    });

    //Reduz o ttl em 1
    pacote.reduzTtl();
    if (pacote.getTtl() == 0){//Se o ttl for 0, entao o tempo de vida do pacote acabou
      return;
    }

    if (pacote.getDestino().equals(this.identificador)){
      mutex.acquire();
      pacoteChegou = true;
      mutex.release();
      return;
    }

    for(Aresta<Roteador> aresta : vizinhos){
      //Ignora o roteador que enviou o pacote
      if (aresta.getDestino().getIdentificador().equals(origemAtual)){
        continue;
      }

      ImageView imagem = new ImageView(new Image("/assets/pomba00.png"));
      imagem.setPreserveRatio(true);
      imagem.setSmooth(true);

      imagem.setFitWidth(25);
      imagem.setFitHeight(25);

      Pacote novoPacote = new Pacote(
          pacote.getOrigem(),
          pacote.getDestino(),
          imagem,
          pacote.getTtl(),
          pacote.getNumSeq(),
          pacote.getIdSimulacao()
      );
      Platform.runLater(() -> {
        imagem.setLayoutX(this.getLayoutX());
        imagem.setLayoutY(this.getLayoutY());

        raiz.getChildren().add(imagem);
        new Thread(() -> {
          try{
            //Envia o pacote
            mutex.acquire();
            if (!pacoteChegou){
               Platform.runLater(() -> {
                contador.set(contador.get() + 1);//Atualiza o contador de pacotes gerados
              });
            }
            double destinoX = aresta.getDestino().getLayoutX();
            double destinoY = aresta.getDestino().getLayoutY();
            mutex.release();

            novoPacote.movimento(destinoX, destinoY);

            //Mata a thread caso a simulacaos seja reiniciada
            SimulacaoContexto ctx = new SimulacaoContexto();
            if (ctx.getIdSimulacao() != this.idSimulacao){
              return;
            }

            //Quando o pacote chegar, o roteamento se inicia no novo roteador
            aresta.getDestino().roteamento(novoPacote, this.getIdentificador());
          }catch(InterruptedException e){
              e.printStackTrace();
          }
        }).start();
      });
    }
  }//Fim do metodo floodingOpcao3

  /* ***************************************************************
	* Metodo: floodingOpcao4
	* Funcao: Implementa o mesmo algoritmo do floodingOpcao3, mas com
            duas otimizacoes:

            Otimizacao 1: agora o numero de sequencia do pacote eh
            relevante. Toda vez que um pacote eh recebido, o numero
            de sequencia eh checado e, se esse numero for menor ou
            igual ao numero ja associado a esse pacote, então ele eh
            destruido. Se nao, entao esse numero de sequencia registrado
            como o mais recente associado ao pacote.

            Otimizacao 2: toda vez que um novo pacote chega, os vizinhos
            imediatos sao checados. Se o destino do pacote for um dos
            vizinhos, entao o peso das arestas eh checado. Se o peso
            da aresta que leva ao destino for o menor dentre todos vizinhos,
            entao o pacote so eh encaminhado por essa aresta, pois esse eh
            o melhor caminho possivel existente. Note que essa otimizacao
            presume que o peso das arestas foi estabelecido tal que, quanto
            menor o peso, melhor o caminho.
	* Parametros: pacote = pacote com informacao de origem, destino e ttl,
                origemAtual = id do roteador de onde o pacote veio.
	* Retorno: void
	*************************************************************** */
  public void floodingOpcao4(Pacote pacote, String origemAtual) throws InterruptedException{
    //Para as imagens de pacotes anteriores nao ficarem paradas na tela, esse metodo eh utilizado.
    //Note que isso nao altera o travamento causado por muitos pacotes gerados, porque o pacote
    //vai ser copiado e enviado de qualquer maneira, de forma que a informacao nao eh perdida.
    Platform.runLater(() -> {
      ImageView imagemAnterior = pacote.getImagem();
      imagemAnterior.setOpacity(0);
    });

    //Reduz o ttl em 1
    pacote.reduzTtl();
    if (pacote.getTtl() == 0){//Se o ttl for 0, entao o tempo de vida do pacote acabou
      return;
    }

    if (pacote.getDestino().equals(this.identificador)){
      mutex.acquire();
      pacoteChegou = true;
      mutex.release();
      return;
    }

    //Id do roteador que enviou o pacote
    String origem = pacote.getOrigem();
    int numSeqNovo = pacote.getNumSeq();
    //Se esse pacote tiver um numero de sequencia ja registrado do seu roteador de origem, entao
    //ele ja foi recebido e enviado aos vizinhos, portanto nao eh necessario envia-lo de novo
    if (mapa.containsKey(origem)) {
      int numSeqAtual = mapa.get(origem);
      if (!(numSeqNovo > numSeqAtual)){
        return;
      }
    }
    //Se esse novo numero de sequencia for maior que o registrado, entao ele eh novo, assim
    //o pacote deve ser enviado aos vizinhos e o numero de sequencia atual deve ser atualizado
    mapa.put(origem, numSeqNovo);

    String destino = pacote.getDestino();
    boolean enviarSoParaODestino = false;
    int menorPeso = 999999;
    int pesoDestinoHipotetico = -1;
    for(Aresta<Roteador> aresta : vizinhos){//Para todos vizinhos
      //Ignora o roteador de onde o pacote chegou veio
      if (aresta.getDestino().getIdentificador().equals(origemAtual)){
        continue;
      }

      //Se o peso da resta for menor ou igual ao menor peso, atualizar o menor peso
      if (aresta.getPeso() <= menorPeso){
        menorPeso = aresta.getPeso();
      }

      //Se o destino do pacote for uma dos vizinhos, registrar seu peso
      if (aresta.getDestino().getIdentificador().equals(destino)){
        pesoDestinoHipotetico = aresta.getPeso();
      }
    }

    //Se a aresta com o menor peso for a mesma que leva ao destino, entao define como verdade
    //uma variavel booleana que forca o roteador a enviar o pacote apenas por essa aresta
    if (menorPeso == pesoDestinoHipotetico){
      enviarSoParaODestino = true;
    }

    for(Aresta<Roteador> aresta : vizinhos){
      //Ignora o roteador que enviou o pacote
      if (aresta.getDestino().getIdentificador().equals(origemAtual)){
        continue;
      }

      if ((enviarSoParaODestino) && !(aresta.getDestino().getIdentificador().equals(destino))){
        continue;
      }

      ImageView imagem = new ImageView(new Image("/assets/pomba00.png"));
      imagem.setPreserveRatio(true);
      imagem.setSmooth(true);

      imagem.setFitWidth(25);
      imagem.setFitHeight(25);

      Pacote novoPacote = new Pacote(
          pacote.getOrigem(),
          pacote.getDestino(),
          imagem,
          pacote.getTtl(),
          pacote.getNumSeq(),
          pacote.getIdSimulacao()
      );
      Platform.runLater(() -> {
        imagem.setLayoutX(this.getLayoutX());
        imagem.setLayoutY(this.getLayoutY());

        raiz.getChildren().add(imagem);
        new Thread(() -> {
          try{
            //Envia o pacote
            mutex.acquire();
            if (!pacoteChegou){
               Platform.runLater(() -> {
                contador.set(contador.get() + 1);//Atualiza o contador de pacotes gerados
              });
            }
            double destinoX = aresta.getDestino().getLayoutX();
            double destinoY = aresta.getDestino().getLayoutY();
            mutex.release();

            novoPacote.movimento(destinoX, destinoY);

            //Mata a thread caso a simulacaos seja reiniciada
            SimulacaoContexto ctx = new SimulacaoContexto();
            if (ctx.getIdSimulacao() != this.idSimulacao){
              return;
            }

            //Quando o pacote chegar, o roteamento se inicia no novo roteador
            aresta.getDestino().roteamento(novoPacote, this.getIdentificador());
          }catch(InterruptedException e){
              e.printStackTrace();
          }
        }).start();
      });
    }
  }//Fim do metodo floodingOpcao4

  /* ***************************************************************
	* Metodo: resetPacoteChegou
	* Funcao: Atualiza a flag do pacote chegou, permitindo que o contador
            continue sendo atualizado após o fim de um ciclo da simulacao.
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
  public void resetPacoteChegou(){
    this.pacoteChegou = false;
  }//Fim do metodo resetPacoteChegou

  /* ***************************************************************
	* Metodo: toString
	* Funcao: Retorna uma representacao em forma de String do objeto.
	* Parametros: Nenhum
	* Retorno: String
	*************************************************************** */
  @Override
  public String toString(){
    return identificador;
  }//Fim do metodo toString
}//Fim da classe Roteador
