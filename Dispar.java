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

  // NOU: Constructor dirigit (apunta directament cap a un objectiu)
  public Dispar(PVector origen, PVector objectiu, boolean esEnemic) {
    this.posicio = origen.copy();
    this.esEnemic = esEnemic;
    this.tamany = 8;

    if (esEnemic) {
      float velocitatBala = 6.0f;
      // Calculem el vector direcció cap a l'objectiu
      PVector dir = PVector.sub(objectiu, origen);
      dir.normalize();
      
      this.velocitatX = dir.x * velocitatBala;
      this.velocitatY = dir.y * velocitatBala;
    } else {
      this.velocitatX = 15;
      this.velocitatY = 0;
    }
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
