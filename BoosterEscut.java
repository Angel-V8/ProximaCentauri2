import processing.core.PApplet;

public class BoosterEscut extends Booster {
  public void mostrar(PApplet app) {
    app.fill(0, 150, 255); // Blau elèctric
    app.noStroke();
    app.rectMode(PApplet.CENTER);
    app.rect(this.posicio.x, this.posicio.y, this.tamany, this.tamany, 5);
    app.fill(255);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.textSize(16);
    app.text("O", this.posicio.x, this.posicio.y - 2);
    app.rectMode(PApplet.CORNER);
  }
}