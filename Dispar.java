import processing.core.PApplet;
import processing.core.PVector;

public class Dispar {

  private PVector posicio;
  private float velocitatX;
  private float velocitatY; // NOU: Velocitat vertical
  private int tamany;
  private boolean esEnemic; 

  public Dispar(PVector origen, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;

    if (esEnemic) {
      this.velocitatX = -6; // Les bales enemigues van un poc més lentes
      // NOU: Variació aleatòria amunt i avall (entre -1.5 i 1.5)
      this.velocitatY = (float)(Math.random() * 3) - 1.5f; 
    } else {
      this.velocitatX = 15; // NOU: Les teues bales van MÉS RÀPIDES (abans 10)
      this.velocitatY = 0;  // Les teues van totalment rectes
    }
  }

  // NOU: Constructor dirigit (apunta un poc desviat per no tindre 100% de punteria) (in-place per evitar GC)
  public Dispar(PVector origen, PVector objectiu, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;

    if (esEnemic) {
      float velocitatBala = 5.0f; // Un poc més lenta per poder esquivar-la (abans 6.0f)
      
      // Afegim una desviació aleatòria de punteria de fins a +/- 40 píxels
      float targetY = objectiu.y + (float)(Math.random() * 80) - 40;
      
      // Calculem la direcció cap a l'objectiu desviat (in-place)
      float dx = objectiu.x - origen.x;
      float dy = targetY - origen.y;
      float dLen = (float)Math.sqrt(dx*dx + dy*dy);
      if (dLen > 0) {
        dx /= dLen;
        dy /= dLen;
      }
      
      this.velocitatX = dx * velocitatBala;
      this.velocitatY = dy * velocitatBala;
    } else {
      this.velocitatX = 15;
      this.velocitatY = 0;
    }
  }

  // NOU: Constructor amb velocitat i desviació customitzades per a atacs especials de caps
  public Dispar(PVector origen, PVector objectiu, float velocitat, float desviacio, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;

    if (esEnemic) {
      float targetY = objectiu.y + (float)(Math.random() * (desviacio * 2)) - desviacio;
      float dx = objectiu.x - origen.x;
      float dy = targetY - origen.y;
      float dLen = (float)Math.sqrt(dx*dx + dy*dy);
      if (dLen > 0) {
        dx /= dLen;
        dy /= dLen;
      }
      this.velocitatX = dx * velocitat;
      this.velocitatY = dy * velocitat;
    } else {
      this.velocitatX = 15;
      this.velocitatY = 0;
    }
  }

  // NOU: Constructor amb vectors de velocitat explícits (per a ventalls, ràdials, etc.)
  public Dispar(PVector origen, float velX, float velY, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;
    this.velocitatX = velX;
    this.velocitatY = velY;
  }

  public void actualitzar() {
    this.posicio.x += this.velocitatX;
    this.posicio.y += this.velocitatY; // NOU: Apliquem el desvio
  }

  public void mostrar(PApplet app) {
    app.pushStyle();
    app.rectMode(PApplet.CENTER);
    
    if (this.esEnemic) {
      // Efecte resplendor (Aura Roja) per a l'enemic
      app.noStroke();
      app.fill(255, 0, 0, 150); 
      app.ellipse(this.posicio.x, this.posicio.y, 24, 12);
      app.fill(255, 255, 150); // Nucli Groguenc/Blanc
      app.ellipse(this.posicio.x, this.posicio.y, 12, 6);
    } else {
      // Efecte resplendor (Aura Cian) per al jugador
      app.noStroke();
      app.fill(0, 200, 255, 150);
      app.ellipse(this.posicio.x, this.posicio.y, 30, 12);
      app.fill(255, 255, 255); // Nucli Blanc pur
      app.ellipse(this.posicio.x, this.posicio.y, 16, 6);
    }
    
    app.popStyle();
  }
  
  public PVector getPosicio() { return this.posicio; }
  public int getTamany() { return this.tamany; }
  public boolean isEsEnemic() { return this.esEnemic; }
}
