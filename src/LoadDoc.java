import java.io.File;
import java.io.IOException;
 
import org.apache.pdfbox.pdmodel.PDDocument; 
import org.apache.pdfbox.pdmodel.PDPage;

public class LoadDoc {
    public static void main(String args[]) throws IOException {
   
        //Loading an existing document 
        File file = new File("C:/Users/mufid/hobby/OCR/Discover-AccountActivity-20230726.pdf"); 
        PDDocument document = PDDocument.load(file); 
          
        System.out.println("PDF loaded"); 
          
        //Adding a blank page to the document 
        document.addPage(new PDPage());  
  
        //Saving the document 
        document.save("C:/Users/mufid/hobby/OCR/Discover-AccountActivity-20230726.pdf");
  
        //Closing the document  
        document.close(); 
          
     }  
    
}
