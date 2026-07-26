/* ***************************************************************
* Autor............: Davi Gabrielli Santos
* Matricula........: 202410855
* Inicio...........: 16/03/2026
* Ultima alteracao.: 27/03/2026
* Nome.............: Controlador Tela Principal
* Funcao...........: Gerencia a tela principal do programa, desenhando
										 na tela e definindo os metodos que garantem o
										 funcionamento correto das opcoes oferecidas nas
										 telas
*************************************************************** */

package controle;

import modelo.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.ResourceBundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.Scene;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;;

public class ControladorTelaPrincipal implements Initializable {
	@FXML private AnchorPane raizPrincipal;
	@FXML private Pane raiz;
	@FXML private Spinner<Integer> spinner;
	@FXML private ToggleGroup opcoes;

	//Opcoes de algoritmos do menu
	@FXML private RadioMenuItem opcao1;
	@FXML private RadioMenuItem opcao2;
	@FXML private RadioMenuItem opcao3;
	@FXML private RadioMenuItem opcao4;

	//Caixas de escolha para roteadores
	@FXML private ComboBox<Roteador> comboBoxDestino;
	@FXML private ComboBox<Roteador> comboBoxOrigem;

	//Texto de pacotes gerados na tela
	@FXML private Label labelPacotesGerados;

	//Coordenadas para geracao de roteadores
	private double centroX = 670.0;
	private double centroY = 370.0;
	private double raio = 275.0;
	private double raioIdentificadores = 310.0;

	//Numero de sequencia dos pacotes (para o algoritmo 4)
	private static int numSeq = 0;

  //Inicia a classe que controla a continuidade da simulacao
	private SimulacaoContexto ctx = new SimulacaoContexto();

	//Inicializa o grafo que representa a subrede e o semaforo
	private Grafo subrede;
	private Semaphore mutex;

	//Contador de pacotes
	private IntegerProperty contadorPacotes;
	
	/* ***************************************************************
	* Metodo: initialize
	* Funcao: Quando esta tela eh carregada, alguns recursos, objetos,
						imagens, etc precisam ser inicializados antes que ela
						seja exibida. Este metodo se encarrega de inicializar
						tais funcionalidades antes da tela ser exibida.
	* Parâmetros: URL location, ResourceBundle resources
	* Retorno: void
	*************************************************************** */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	  //Carrega a fonte customizada
		Font font = Font.loadFont(
			getClass().getResource("/assets/EvilEmpire.otf").toExternalForm(),
			20
		);
		//Inicializa as opcoes
		SpinnerValueFactory<Integer> listaDeValores =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1,20);

		listaDeValores.setValue(5);
		spinner.setValueFactory(listaDeValores);
		spinner.setEditable(false);

		opcao1.setUserData(1);
		opcao2.setUserData(2);
		opcao3.setUserData(3);
		opcao4.setUserData(4);

		//Inicializa o contexto da simulacao
		ctx.setIdSimulacao(0);

		//Chama os construtores
		construirRoteadores();
		construirConexoes();
		construirComboBox();
	}

	/* ***************************************************************
	* Metodo: iniciar
	* Funcao: Inicia a simulacao, enviando os primeiros pacotes.
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	@FXML
	public void iniciar(){
		//Verifica quantas threads ativas existem
		int qtd = Thread.getAllStackTraces().keySet().size();

		//Se mais que 9 threads estiverem ativas, nao permitir o inicio do programa (simulacao nao acabou)
		if (qtd > 9){
			mostrarAlerta("Atencao", "Simulacao nao finalizada", "Espere que a simulacao atual tenha fim antes de enviar outro pacote."+
			" Caso a simulacao nao tenha fim, clique no botao de reiniciar.");
			return;
		}

		Roteador origem = comboBoxOrigem.getValue();
		Roteador destino = comboBoxDestino.getValue();

		if (origem == null || destino == null) {
			mostrarAlerta("Atencao", "Selecao incompleta", "Selecione a origem e o destino antes de iniciar a simulacao.");
			return;
    }

		//Se o destino e a origem forem os mesmos, nao ha porque enviar pacotes
    if (origem == destino){
			return;
    }

		//Reinicia a flag que indica que o pacote chegou para destravar a contagem de pacotes
		origem.resetPacoteChegou();
		contadorPacotes.set(0);

		ImageView imagem = new ImageView(new Image("/assets/pomba00.png"));
		imagem.setPreserveRatio(true);
		imagem.setSmooth(true);

		imagem.setFitWidth(25);
		imagem.setFitHeight(25);

		imagem.setLayoutX(origem.getLayoutX());
		imagem.setLayoutY(origem.getLayoutY());

		ArrayList<Roteador> roteadores = subrede.getVertices();

		int opcaoDeRoteamento = (int) opcoes.getSelectedToggle().getUserData();
		int ttl = spinner.getValue() + 1;

		//Define o algoritmo que sera usado para todos roteadores
		for(Roteador roteador : roteadores){
			roteador.setRoteamento(opcaoDeRoteamento);
		}


		int numSeqTemp = numSeq;
		int idAtual = ctx.getIdSimulacao();
		new Thread(() -> {
			try{
				origem.roteamento(new Pacote(origem.getIdentificador(),destino.getIdentificador(),imagem,ttl,numSeqTemp, idAtual), origem.getIdentificador());
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}).start();
		numSeq++;
	}//Fim do metodo iniciar

  /* ***************************************************************
	* Metodo: reiniciar
	* Funcao: Encerra as threads atuais da simulacao e limpa a tela,
	          preparando a cena para a reconstrucao da subrede, que
					  eh realizada pelo metodo reiniciarAux
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	@FXML
	public void reiniciar(){
		ctx.setIdSimulacao(ctx.getIdSimulacao() + 1);
		new Thread(() -> {
			try{
        //Enquanto as threads dos pacotes estiverem ativas (normalmente o numero eh 9, mas esta thread tambem conta)
				while(Thread.getAllStackTraces().keySet().size() > 10){
					Thread.sleep(50);//Aguarda
				}
			  Platform.runLater(() -> {
			    //Threads acabaram, entao limpa a tela
          raiz.getChildren().clear();
          //Chama metodo que reinicia a subrede e os roteadores
          reiniciarAux();
        });
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}).start();
	}

	/* ***************************************************************
	* Metodo: reiniciarAux
	* Funcao: Reconstroe a subrede, reiniciando os roteadores e as arestas
	          que os conectam
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	public void reiniciarAux(){
    //Recria tudo
    construirRoteadores();
    construirConexoes();

    //Atualiza as caixas de escolha (o usuario pode ter alterado o backbone)
    comboBoxOrigem.getItems().clear();
    comboBoxDestino.getItems().clear();
    construirComboBox();
	}//Fim do metodo reiniciarAux

	/* ***************************************************************
	* Metodo: contruirRoteadores
	* Funcao: Coloca os roteadores na tela de acordo com a descricao do
						arquivo "backbone.txt", encontrado na raiz na pasta do
						projeto. A representacao logica da sub-rede tambem eh
						inicializada.
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	public void construirRoteadores(){
		subrede = new Grafo();
		mutex = new Semaphore(1);

		String caminhoDoArquivo = "backbone.txt";
		contadorPacotes = new SimpleIntegerProperty(0);
		labelPacotesGerados.textProperty().bind(contadorPacotes.asString());

		try{
			BufferedReader leitor = new BufferedReader(new FileReader(caminhoDoArquivo));
			String linha;

			//Extrai a primeira linha do arquivo para verificar quantos roteadores devem ser construidos
			linha = leitor.readLine();
			String[] partes = linha.split(";");
			int qtdRoteadoresParaAdicionar = Integer.parseInt(partes[0]);

			//Calcula distancia ideal entre roteadores para manter simetria
			double espacoEntreRoteadores = 360.0 / (double) qtdRoteadoresParaAdicionar;
			//Ajusta o angulo a partir de 270 graus (topo do círculo)
			double parametroOriginal = 270.0;

			int contador = 1;
			while(qtdRoteadoresParaAdicionar > 0){
				parametroOriginal += espacoEntreRoteadores;

				//Inicializa as imagens do roteadores
				ImageView imagem = new ImageView(escolherImagemAleatoria());
				imagem.setPreserveRatio(true);
				imagem.setSmooth(true);

				//Define a posicao do roteador em formato circular de acorco com as coordenadas polares
				double layoutX = centroX + raio * Math.cos(Math.toRadians(parametroOriginal));
				double layoutY = centroY + raio * Math.sin(Math.toRadians(parametroOriginal));

				//Define o tamanho da imagem que representa o roteador
				imagem.setFitWidth(64);
				imagem.setFitHeight(64);

				//Define a posicao da imagem
				imagem.setLayoutX(layoutX - (imagem.getFitWidth() / 2));
				imagem.setLayoutY(layoutY - (imagem.getFitHeight() / 2));

				//Embeleza a imagem
				DropShadow sombra = new DropShadow();
				sombra.setRadius(5);
				sombra.setOffsetX(2);
				sombra.setOffsetY(2);
				sombra.setColor(Color.color(0, 0, 0, 0.5));

				imagem.setEffect(sombra);

				//Define a posicao do roteador em formato circular de acorco com as coordenadas polares
				double layoutXIdentificador = centroX + raioIdentificadores * Math.cos(Math.toRadians(parametroOriginal));
				double layoutYIdentificador = centroY + raioIdentificadores * Math.sin(Math.toRadians(parametroOriginal));

				//Cria o texto que identifica cada roteador e atrela ele as imagens
				Text identificador = new Text(50,50,String.valueOf(contador));
				identificador.setX(layoutXIdentificador);
				identificador.setY(layoutYIdentificador);
				identificador.setFont(Font.loadFont(
						getClass().getResource("/assets/EvilEmpire.otf").toExternalForm(),
						20
				));

				identificador.setFill(Color.WHITE);
				identificador.setStroke(Color.BLACK);
				identificador.setStrokeWidth(0.5);

				//Adiciona a imagem no AnchorPane do stage, fazendo com que ela apareca na tela
				raiz.getChildren().add(imagem);
				raiz.getChildren().add(identificador);

				//Adiciona o roteador como vertice na representação logica da sub-rede (grafo)
				Roteador r = new Roteador(imagem, String.valueOf(contador), 1, raiz, mutex, contadorPacotes);
				subrede.addVertice(r);

				//Passa a a lista de adjacencias de cada roteador
				r.addVizinhos(subrede.getLista(contador - 1));

				//Define o id da simulacao
				r.setIdSimulacao(ctx.getIdSimulacao());

				qtdRoteadoresParaAdicionar--;
				contador++;
			}
		}
		catch(FileNotFoundException e){
			System.out.println("Arquivo não encontrado.");
		}
		catch(IOException e){
			System.out.println("Algo deu errado.");
		}
	}//Fim do metodo construirRoteadores

	/* ***************************************************************
	* Metodo: construirConexoes
	* Funcao: Desenha as conexoes entre os roteadores na tela. Tambem
						preenche o grafo que determina cada relacao entre os
						roteadores
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	public void construirConexoes(){
		String caminhoDoArquivo = "backbone.txt";

		try{
			BufferedReader leitor = new BufferedReader(new FileReader(caminhoDoArquivo));
			String linha;

			//Extrai a primeira linha do arquivo porque ela não eh necessaria para a construcao das conexoes
			linha = leitor.readLine();

			while((linha = leitor.readLine()) != null){
				String[] partes = linha.split(";");

				if (partes[0].equals("")){
					continue;
				}

				//Retira cada informacao da linha do backbone
				int origem = Integer.parseInt(partes[0]) - 1;
				int destino = Integer.parseInt(partes[1]) - 1;

				//Para evitar erros na decricao do backbone
				String[] partePeso = partes[2].split(" ");
				int peso = Integer.parseInt(partePeso[0]);

				//Adiciona a aresta na subrede
				subrede.addAresta(origem, destino, peso);

				//Define as coordenadas de cada conexao na tela
				double origemLayoutX = subrede.getVertice(origem).getLayoutX();
				double origemLayoutY = subrede.getVertice(origem).getLayoutY();

				double destinoLayoutX = subrede.getVertice(destino).getLayoutX();
				double destinoLayoutY = subrede.getVertice(destino).getLayoutY();

				//Desenha a conexao entre os roteadores na tela
				Line conexao = new Line();
				conexao.setStartX(origemLayoutX);
				conexao.setStartY(origemLayoutY);
				conexao.setEndX(destinoLayoutX);
				conexao.setEndY(destinoLayoutY);
				conexao.setStroke(Color.BLACK);//Cor
				conexao.setStrokeWidth(3);

				//Cria o texto que identifica o peso da aresta na tela
				Text identificador = new Text(50,50,String.valueOf(peso));
				identificador.setX((Math.abs(origemLayoutX + destinoLayoutX)) / 2);
				identificador.setY((Math.abs(origemLayoutY + destinoLayoutY)) / 2);
				identificador.setFont(Font.loadFont(
					getClass().getResource("/assets/EvilEmpire.otf").toExternalForm(),
					15
				));
				identificador.setFill(Color.WHITE);
				identificador.setStroke(Color.BLACK);
				identificador.setStrokeWidth(0.5);

				//Adiciona a conexao e o texto
				raiz.getChildren().add(conexao);
				raiz.getChildren().add(identificador);
				conexao.toBack();
			}
		}
		catch(FileNotFoundException e){
			System.out.println("Arquivo nao encontrado.");
		}
		catch(IOException e){
			System.out.println("Algo deu errado.");
		}
	}//Fim do metodo construirConexoes

	/* ***************************************************************
	* Metodo: editarSubrede
	* Funcao: Abre uma area de texto na tela para permitir que o usuario
	          altere o arquivo de define a estrutura da subrede
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	@FXML
	public void editarSubrede(){
	  //Pane de fundo
		Pane overlay = new Pane();
		overlay.setPrefSize(600, 400);
		overlay.setStyle("");

		//Bloqueia interacao com o resto da interface
		overlay.setPickOnBounds(true);

		//Cria interface
		VBox caixa = new VBox(10);
		caixa.setPrefSize(600, 500);
		caixa.setStyle(
			"-fx-background-color: #6ec6ff;" +
			"-fx-padding: 15;" +
			"-fx-background-radius: 10;"
		);

		//Centraliza na tela
		caixa.setLayoutX((raizPrincipal.getWidth() - 600) / 2);
		caixa.setLayoutY((raizPrincipal.getHeight() - 500) / 2);

		//Area onde o texto eh carregado para que o usuario possa edita-lo
		TextArea areaTexto = new TextArea();
		areaTexto.setWrapText(true);
		areaTexto.setPrefHeight(400);
		areaTexto.setStyle("-fx-font-size: 18px;");

		//Carregar conteudo do arquivo
		areaTexto.setText(lerArquivo());

		//Botoes
		Button btnSalvar = new Button("Salvar");
		Button btnCancelar = new Button("Cancelar");

		btnSalvar.getStyleClass().add("botao");
		btnCancelar.getStyleClass().add("botao");

    //Caixa onde os botoes se localizam
		HBox botoes = new HBox(10, btnSalvar, btnCancelar);
		botoes.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);;

		//Acoes dos botoes
		btnCancelar.setOnAction(e -> {
			raiz.getChildren().remove(overlay);
		});

		btnSalvar.setOnAction(e -> {
			String novoConteudo = areaTexto.getText();

			escreverArquivo(novoConteudo);

			raiz.getChildren().remove(overlay);

			reiniciar();
		});

		//Monta interface
		caixa.getChildren().addAll(areaTexto, botoes);
		overlay.getChildren().add(caixa);

		//Adiciona na tela
		raiz.getChildren().add(overlay);
	}//Fim do metodo editarSubrede

  /* ***************************************************************
	* Metodo: lerArquivo
	* Funcao: Retorna uma string do texto presente no backbone.txt, que
						representa a estrutura da subrede
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	public String lerArquivo(){
	  String caminhoDoArquivo = "backbone.txt";
    String texto = "";

		try{
			BufferedReader leitor = new BufferedReader(new FileReader(caminhoDoArquivo));
			String linha = leitor.readLine();

			while (linha != null){
			  texto += linha + "\n";
			  linha = leitor.readLine();
			}

		}
		catch(FileNotFoundException e){
			System.out.println("Arquivo não encontrado.");
		}
		catch(IOException e){
			System.out.println("Algo deu errado.");
		}
		return texto;
	}//Fim do metodo lerArquivo

  /* ***************************************************************
	* Metodo: escreverArquivo
	* Funcao: Recebe uma a string do texto presente na area de texto
	          definida no metodo editarSubrede e a escreve no arquivo
	          backbone.txt que representa a estrutura da subrede
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	public void escreverArquivo(String conteudo){
		String caminhoDoArquivo = "backbone.txt";

		try{
			BufferedWriter escritor = new BufferedWriter(new FileWriter(caminhoDoArquivo));

			escritor.write(conteudo);

			escritor.close();
		}
		catch(IOException e){
			System.out.println("Erro ao escrever no arquivo.");
		}
	}//Fim do metodoe escreverArquivo


	/* ***************************************************************
	* Metodo: construirConexoes
	* Funcao: Inicializa as caixas de escolhas com os vertices da subrede
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	public void construirComboBox(){
		comboBoxOrigem.getItems().addAll(subrede.getVertices());
		comboBoxDestino.getItems().addAll(subrede.getVertices());
	}//Fim do metodo construirComboBox

	/* ***************************************************************
	* Metodo: mostrarAlerta
	* Funcao: Exibe um alerta na tela
	* Parametros: titulo = titulo do alerta, cabecalho = cabecalho do alerta
	              mensagem = mensagem do alerta
	* Retorno: void
	*************************************************************** */
	public static void mostrarAlerta(String titulo, String cabecalho, String mensagem) {
    Alert alerta = new Alert(AlertType.WARNING);
    alerta.setTitle(titulo);
    alerta.setHeaderText(cabecalho);

    Label label = new Label(mensagem);
    label.setWrapText(true);

		//As vezes, um bug faz com que o alerta apareca em uma janela minuscula, sendo
		//impossivel de ler o que esta escrito ou mesmo de percebe-lo. Para resolver isso
		//a propriedade de resizable deve ser definida como true e depois valores minimos para
		//a altura e largura sao estabelecidos
    alerta.setResizable(true);
    alerta.getDialogPane().setContent(label);
    alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alerta.getDialogPane().setMinWidth(300);

    alerta.showAndWait();
	}//Fim do metodo mostrarAlerta

	/* ***************************************************************
	* Metodo: escolherImagemAleatoria
	* Funcao: Escolhe uma dentre 4 imagens para representar o roteador na tela
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	private Image escolherImagemAleatoria() {
    String[] caminhos = {
			"/assets/tankblue.png",
			"/assets/tank_left.png",
			"/assets/tank_red_right.png",
			"/assets/tank_red_left.png"
    };

    Random random = new Random();
    int indice = random.nextInt(caminhos.length);

    return new Image(caminhos[indice]);
	}//Fim do metodo escolherImagemAleatoria

	/* ***************************************************************
	* Metodo: mostrarAjuda
	* Funcao: Define o texto da ajdua e chama a funcao que mostra a ajuda
	          na tela
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	@FXML
	public void mostrarAjuda() {
	 String titulo = "Instrucoes";
   String descricao = "Este eh um simulador de roteamento por inundacao."+
											" A subrede eh definida pelo arquivo \"backbone.txt\""+
											", que se encontra na raiz do diretório do projeto."+
											" Para definir a subrede, utilize a primeira linha do arquivo"+
											" para definir quantos nos (representados por tanques de guerra)"+
											" estarao presentes na subrede. Nas linhas seguintes, as conexoes"+
											" entre nos sao definidas da seguinte forma: NO1;NO2;PESO\n"+
											"Qualquer erro na definicao da subrede pode ocasionar no mal funcionamento"+
											" deste programa. OBS: nao precisa reabrir o programa para refazer a subrede."+
											" Basta clicar no botao de reiniciar que ela sera reconstruida.\n\n"+
											"Instrucoes:\n"+
											"1.Clique nas opcoes para selecionar um algoritmo e o TTL do pacote\n"+
											"2.Selecione um roteador de origem e um roteador de destino\n"+
											"3.Clique no botao de play\n"+
											"4.Observe a simulacao acontecer e a quantidade de pacotes gerados\n"+
											"5.Quando a simulacao se encerrar, repita o processo. Caso a simulacao"+
											" nao se encerre (pode ocorrer nos algoritmos 1 e 2 ou no 3 em caso de"+
											"ttl muito alto), clique no botao de reiniciar.";
   exibirInfo(titulo, descricao);
	}//Fim do metodo mostrarAjuda

	/* ***************************************************************
	* Metodo: mostrarAlgoritmos
	* Funcao: Define o texto de ajuda e chama a funcao que mostra o texto
	          explicando os algoritmos na tela
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	@FXML
	public void mostrarAlgoritmos() {
	 String titulo = "Algoritmos";
   String descricao = "Algoritmo de inundacao 1: o pacote eh enviado a TODOS os roteadores"+
											" adjacentes, sem excecao.\n\n"+
											"Algoritmo de inundacao 2: o pacote eh enviado a todos roteadores"+
											" adjacentes, com excecao ao roteador de onde o pacote veio\n\n"+
											"Algoritmo de inundacao 3: o pacote eh enviado a todos roteadores"+
											"adjacentes, com excecao ao roteador de onde o pacote veio. Os pacotes"+
											" tambem possuem um TTL. Toda vez que um pacote passa por um roteador,"+
											" o valor do TTL eh subtraido em 1. Se chegar a 0, o pacote eh destruido.\n\n"+
											"Algoritmo de inundacao 4: igual ao algoritmo 3, mas com duas otimizacoes.\n"+
											" Otimizacao 1: agora, o numero de sequencia dos pacotes eh relevante. Toda vez"+
											" que um roteador recebe um pacote, o numero de sequencia dele eh atualizado relativo"+
											" ao seu roteador de origem. Assim, caso outro pacote com numero de sequencia igual"+
											" ou inferior ao numero de sequencia atual daquele roteador de origem seja recebido, ele eh imediatamente"+
											" destruido, pois se trata de informacao ultrapassada.\n"+
											"Otimizacao 2: os roteadores verificam se o roteador de destino daquele pacote"+
											" esta presente na sua lista de adjacencia imediata. Se sim, e se o peso da aresta"+
											" que o conecta a este destino for a menor dentre todas adjacencias, entao o pacote"+
											" eh enviado apenas aquele roteador, pois essa eh a melhor rota possivel naquele ponto."+
											" Observe que essa otimizacao presume que o peso das arestas seja uma metrica tal que, quanto"+
											" menor o valor, melhor a conexao";
   exibirInfo(titulo, descricao);
	}//Fim do metodo mostrarAlgoritmos

	/* ***************************************************************
	* Metodo: exibirInfo
	* Funcao: Exibe um alerta de informacao na tela, dado um titulo e
	          uma descricao.
	* Parametros: titulo = titulo do alerta, descricao = descricao do
	              alerta
	* Retorno: void
	*************************************************************** */
	public void exibirInfo(String titulo, String descricao){
		Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle(titulo);

    TextArea areaTexto = new TextArea();
    areaTexto.setText(descricao);
    areaTexto.setWrapText(true);
    areaTexto.setEditable(false);

		//Tamanho da fonte
    areaTexto.setStyle(
        "-fx-font-size: 16px;"
    );

    //Tamanho do painel
    areaTexto.setPrefWidth(500);
    areaTexto.setPrefHeight(350);

    dialog.getDialogPane().setContent(areaTexto);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    //Tamanho mínimo do painel do dialog
    dialog.getDialogPane().setMinWidth(550);
    dialog.getDialogPane().setMinHeight(400);

    dialog.setResizable(true);

    dialog.showAndWait();
	}//Fim do metodo exibirInfo

	/* ***************************************************************
	* Metodo: encerrar
	* Funcao: Encerra o programa.
	* Parametros: Nenhum
	* Retorno: void
	*************************************************************** */
	@FXML
	public void encerrar() {
    Platform.exit();
    System.exit(0);
	}//Fim do metodo encerrar
}//Fim da classe ControladorTelaPrincipal
