import processing.core.PApplet;
import processing.core.PVector;

public class Dispar {

  private PVector posicio;
  private float velocitatX;
  private float velocitatY; // Velocitat vertical
  private int tamany;
  private boolean esEnemic; 

  public Dispar(PVector origen, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;

    if (esEnemic) {
      this.velocitatX = -6; // Les bales enemigues van un poc més lentes
      // Variació aleatòria vertical (entre -1.5 i 1.5)
      this.velocitatY = (float)(Math.random() * 3) - 1.5f; 
    } else {
      this.velocitatX = 15; // Les bales del jugador van més ràpides
      this.velocitatY = 0;  // Les del jugador van rectes
    }
  }

  // Constructor per a dispars dirigits cap a un objectiu
  public Dispar(PVector origen, PVector objectiu, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;

    if (esEnemic) {
      float velocitatBala = 5.0f; // Velocitat de la bala enemiga
      
      // Desviació aleatòria de punteria de fins a +/- 40 píxels
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

  // Constructor amb velocitat i desviació personalitzades per a atacs especials de caps
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

  // Constructor amb vectors de velocitat explícits (per a ventalls, ràdials, etc.)
  public Dispar(PVector origen, float velX, float velY, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;
    this.velocitatX = velX;
    this.velocitatY = velY;
  }

  public void actualitzar() {
    this.posicio.x += this.velocitatX;
    this.posicio.y += this.velocitatY; // Apliquem la velocitat vertical
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
