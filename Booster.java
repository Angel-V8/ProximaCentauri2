import processing.core.PApplet;
import processing.core.PVector;

public abstract class Booster {
  protected PVector posicio;
  protected int tamany;
  protected float velocitat;

  public Booster() {
    this.tamany = 30;
    this.velocitat = 3;
    float y = (float)(Math.random() * 500) + 50;
    this.posicio = new PVector(850, y);
  }

  public void actualitzar() {
    this.posicio.x -= this.velocitat;
  }

  public abstract void mostrar(PApplet app);

  public PVector getPosicio() { return this.posicio; }
  public int getTamany() { return this.tamany; }
}