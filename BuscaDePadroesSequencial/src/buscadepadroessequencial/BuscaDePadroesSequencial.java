/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buscadepadroessequencial;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

/**
 *
 * @author deric
 */
public class BuscaDePadroesSequencial {

    /**
     * @param args the command line arguments
     */
    
    //  printa todas as ocorrencias de padroes
    public static void search(String texto, String padrao) 
    { 
  
        // Cria uma string concatenada "Padrao $ Texto" 
        String concat = padrao + "$" + texto; 
  
        int tam = concat.length(); 
  
        int Z[] = new int[tam]; 
  
        // Constroi array Z  
        getZarr(concat, Z); 
        
        // agora faz o loop no array até coincidir
        /*for(int i = 0; i < tam; ++i){ 
  
            // se Z[i] (região coincidida) é igual ao padrão 
            // printa a posição em que o padrão foi encontrado 
  
            if(Z[i] == padrao.length()){ 
                System.out.println("Padrão encontrado na posição: "
                              + (i - padrao.length() - 1)); 
            } 
        }*/ 
    } 
    
    // Preenche o array Z com a string dada 
    private static void getZarr(String str, int[] Z) { 
  
        int n = str.length(); 
          
        // [L,R] forma um intervalo que coincide com 
        // o prefixo da string 
        int L = 0, R = 0; 
  
        for(int i = 1; i < n; ++i) { 
  
            if(i > R){ 
  
                L = R = i; 
  
                // R-L = 0 no começo, então começamos a  
                // checar pela posição 0. Por exemplo, 
                // para a string "ababab" e i = 1, o valor de R 
                // continua 0 e Z[i] vira 0. Para a string 
                // "aaaaaa" e i = 1, Z[i] e R viram 5 
  
                while(R < n && str.charAt(R - L) == str.charAt(R)) 
                    R++; 
                  
                Z[i] = R - L; 
                R--; 
  
            } 
            else{ 
  
                // k = i-L entao k corresponde ao numero 
                // que coincide no intervalo de [L,R]. 
                int k = i - L; 
  
                // se Z[k] é menor que o intervalo dado 
                // entao Z[i] será igual a Z[k]. 
                // Por exemplo, str = "ababab", i = 3, R = 5 
                // e L = 2 
                if(Z[k] < R - i + 1) 
                    Z[i] = Z[k]; 
  
                // Por exemplo str = "aaaaaa" e i = 2, R é 5, 
                // L é 0 
                else{ 
  
  
                // senão comece pelo R e cheque manualmente 
                    L = i; 
                    while(R < n && str.charAt(R - L) == str.charAt(R)) 
                        R++; 
                      
                    Z[i] = R - L; 
                    R--; 
                } 
            } 
        } 
    } 
    
    public static void main(String[] args) {
    
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
