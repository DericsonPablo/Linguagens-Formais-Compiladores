package trabpart2;

public class CC0  {
    
    public CC0(String filename) {
        Scanner myScanner = new Scanner(filename);
        Parser myParser = new Parser((Scanner) myScanner);
        ((Parser) myParser).Parse();
        System.out.println(((Parser) myParser).errors.count + " errors detected");
        System.out.println("************");
    }
    
    
    
}