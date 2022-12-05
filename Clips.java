package clips.agents;
//javac -cp lib\CLIPSJNI.jar -d classes src\examples\clips\Clips.java
//java -cp lib\CLIPSJNI.jar;classes clips.agents.Clips 
 
//import jade.core.Agent;
//import jade.core.behaviours.Behaviour;
import net.sf.clipsrules.jni.*;

public class Clips{
  
  private static Environment clips;
  public void Clips(){}
  
  public static class Market{
    public void Market(){}
    public void load(){
      clips = new Environment();
      try{
        clips.clear();        
        clips.eval("(batch "+"./src/examples/clips/clips/market/run.clp"+")");
        
        System.out.println("Market load"); 
      }catch(CLIPSException e){ 
        e.printStackTrace();
      }
    }
  }
  public static class Person{
    public void Person(){}
    public void load(){
      try{
        clips.clear();
        clips.eval("(batch "+"./src/examples/clips/clips/persons/run-persons.clp"+")");
        System.out.println("Persons load"); 
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  public static class Product{
    public void Product(){}
    public void load(){   
      try{
      clips.clear();
      clips.eval("(batch "+"./src/examples/clips/clips/prodcust/run-prodcust.clp"+")");
      System.out.println("Product load");   
    }catch(Exception e){
      e.printStackTrace();
    }
    }
  
  }
  

    public static void main(String[] args) {
      Environment clipss;
      Market m = new Market();
      m.load();
      Person p = new Person();
      p.load();
      Product d = new Product();
      d.load();

     }

}