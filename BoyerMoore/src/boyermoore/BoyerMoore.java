/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boyermoore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author deric
 */
public class BoyerMoore {

    /**
     * @param args the command line arguments
     */
     static int NO_DE_CHARS = 256; 
       
    //função para pegar o valor máximo entre dois inteiros
     static int max (int a, int b) { return (a > b)? a: b; } 
  
     //Função de BoyerMoore
     static void badCharHeuristic( String str, int tam,int badchar[]) 
     { 
      int i; 
  
      // Inicializa todas as ocorrencias com -1 
      for (i = 0; i < NO_DE_CHARS; i++) 
           badchar[i] = -1; 
  
      // Preenche o valor atual da ultima ocorrencia de um caracter
      for (i = 0; i < tam; i++) 
           badchar[str.charAt(i)] = i; 
     } 
  
     /*Função base para chamar a função de BoyerMoore */
     static void search( String texto,  String padrao) 
     { 
      int m = padrao.length(); // variavel m com tamanho da string do padrao a ser encontrado
      int n = texto.length(); // variavel n com tamanho da string do texto base
  
      int badchar[] = new int[NO_DE_CHARS]; 
  
      /* Chamada da função de BoyerMoore */
      badCharHeuristic(padrao, m, badchar); 
  
      int s = 0;  // s será a posição em que a sequencia dos caracteres procurados estão situados no texto

      // Enquanto a posição for menor ou igual que o tamanho do texto subtraido do tamanho do padrao
      while(s <= (n - m)) 
      { 
          int j = m-1; 
  
          /* Continua reduzindo o index j enquanto ainda está se coincidindo o padrao com o texto */
          while(j >= 0 && padrao.charAt(j) == texto.charAt(s+j)) 
              j--; 
  
          /* Se o padrão está presente na posição atual, então o index j virará -1 depois do loop acima */
          if (j < 0) 
          { 
              //System.out.println("Padrão encontrado na posição = " + s); 
  
              
                 /* Mude o padrão para que o próximo caractere no texto
                      alinhe-se com a última ocorrência do padrão.
                A condição s + m <n é necessária para o caso quando
                   padrão ocorre no final do texto  
                */
              s += (s+m < n)? m-badchar[texto.charAt(s+m)] : 1; 
  
          } 
  
          else
              /* Mude o padrão para que o caractere no texto
               alinhe-se com a última ocorrência do padrão. 
               A função max é usada para garantir que obtemos um resultado positivo
               de mudança. Podemos ter uma mudança negativa se a última ocorrência
               de caracter no padrão está no lado direito do
               caracter atual.*/
              s += max(1, j - badchar[texto.charAt(s+j     )]); 
      } 
     } 
  
     /* Driver program to test above function */
    public static void main(String []args) { 
          
        String texto; 
        String padrao;
        int tamanhoTexto=0;
        int k = 100; //ESCOLHA DO K (TAMANHO DA STRING DO PADRAO A SER ENCONTRADA)
        Random numeroAleatorio = new Random();
        int posicaoAleatoria;
        
        try {
            FileReader arq = new FileReader("C:\\Users\\deric\\Documents\\NetBeansProjects\\BuscaDePadroesSequencial\\hgtd\\Species1.fasta");
            BufferedReader lerArq = new BufferedReader(arq);

            texto = lerArq.readLine(); // lê a primeira linha
            
            // a variável "linha" recebe o valor "null" quando o processo
            // de repetição atingir o final do arquivo texto
            while (lerArq.ready()) {
              //System.out.printf("%s\n", texto);
              
              tamanhoTexto++;
              texto += lerArq.readLine();// lê da segunda até a última linha
              
            } 
            //System.out.println("tamanhotexto" +tamanhoTexto);
            
            String texto2;
            int i = 1;
            while(i<11){
                int cont= 2;
                System.out.println("ITERAÇÃO "+i+"= "); //INDICA QUAL ITERAÇÃO DO FOR ESTÁ
                posicaoAleatoria = numeroAleatorio.nextInt(tamanhoTexto);
                System.out.println("posicao da string escolhida para ser encontrada: "+posicaoAleatoria);
                //System.out.println("texto: "+texto);
                padrao= texto.substring(posicaoAleatoria,posicaoAleatoria+k);
                System.out.println("padrão a ser encontrado de acordo com o k "+k+": "+padrao);
                long tempoInicial = System.currentTimeMillis(); //PEGA TEMPO ATUAL
                while(cont < 12){  
                    FileReader leitor = new FileReader("C:\\Users\\deric\\Documents\\NetBeansProjects\\BuscaDePadroesSequencial\\hgtd\\Species" + cont + ".fasta");
                    System.out.println("lendo arq: "+cont);
                    BufferedReader lerArqu = new BufferedReader(leitor);
                    texto2 = lerArqu.readLine(); //LENDO PRIMEIRA LINHA DO ARQUIVO ATUAL
                    while (lerArqu.ready()) {
                        texto2 += lerArqu.readLine(); //MANTENDO PRIMEIRA LINHA LIDA E ARMAZENANDO AS OUTRAS
                        if (texto2 == null) {
                                break; //SE NAO FOI LIDO NADA 
                        }
                    }
                    //System.out.println("terminei de ler");
                    System.out.println("procurando padrao");
                    search(texto2, padrao); //FAZ O METODO PARA ACHAR O PADRAO ESCOLHIDO NO SPECIES1.FASTA NO ARQUIVO ATUAL ARMAZENADO EM TEXTO2
                    //System.out.println("terminou de procurar");
                    cont++; //passando pro prox arquivo
                }
                long tempoTotal = System.currentTimeMillis() - tempoInicial; //TEMPO TOTAL: TEMPO ATUAL SUBTRAIDO DO TEMPO INICIAL ARMAZENADO
     
                System.out.println("o metodo executou em " +tempoTotal);
            i++;
            }
            
            
            arq.close();
            
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n",
            e.getMessage());
        }
 
        
        
        
    }
    
        
} 
    

