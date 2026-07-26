/* ***************************************************************
* Autor............: Davi Gabrielli Santos
* Matricula........: 202410855
* Inicio...........: 18/03/2026
* Ultima alteracao.: 27/03/2026
* Nome.............: Aresta
* Funcao...........: Classe que serve para representar uma aresta
                     em um grafo, contendo o vertice de destino e
                     o peso da aresta.
*************************************************************** */

package modelo;

public class Aresta<Roteador>{

  public Roteador destino;
  public int peso;

  /* ***************************************************************
	* Metodo: Aresta
	* Funcao: Construtor da classe
	* Parametros: destino = vertice de destino da aresta, peso = peso
                da aresta
	* Retorno: Objeto da classe Aresta
	*************************************************************** */
  public Aresta(Roteador destino, int peso){
    this.destino = destino;
    this.peso = peso;
  }//Fim do metodo Aresta

  /* ***************************************************************
	* Metodo: getDestino
	* Funcao: Retorna o roteador atrelado a essa aresta (eh uma aresta direcionada)
	* Parametros: Nenhum
	* Retorno: Objeto da classe Roteador
	*************************************************************** */
  public Roteador getDestino(){
    return destino;
  }//Fim do metodo getDestino

  /* ***************************************************************
	* Metodo: getPeso
	* Funcao: Retorna o peso da aresta
	* Parametros: destino = vertice de destino da aresta, peso = peso
                da aresta
	* Retorno: int
	*************************************************************** */
  public int getPeso(){
    return this.peso;
  }//Fim do metodo getPeso

  /* ***************************************************************
	* Metodo: toString
	* Funcao: Retorna uma representacao em forma de String do objeto.
	* Parametros: Nenhum
	* Retorno: String
	*************************************************************** */
  public String toString(){
    return this.destino.toString();
  }//Fim do metodo toString
}//Fim da classe Aresta
