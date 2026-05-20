import processing.core.PApplet;

public class BoosterVida extends Booster {
  public void mostrar(PApplet app) {
    app.fill(0, 255, 0);
    app.noStroke();
    app.rectMode(PApplet.CENTER);
    app.rect(this.posicio.x, this.posicio.y, this.tamany, this.tamany, 5);
    app.fill(255);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.textSize(20);
    app.text("+", this.posicio.x, this.posicio.y - 3);
    app.rectMode(PApplet.CORNER);
  }
}