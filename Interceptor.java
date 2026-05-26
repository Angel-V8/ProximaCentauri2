import processing.core.PApplet;
import processing.core.PVector;

public class Interceptor extends Enemic {
    
  private float yInicial;
  private float angle;
  
  protected int temporizador;
  protected int cooldown;
  private PVector posicioJugador; // Referència a la posició del jugador

  public Interceptor(PVector posicioJugador) {
    super();
    this.velocitat = 2.5f;
    this.vel = this.direccio.copy().mult(velocitat);
    this.temporizador = 0;
    this.cooldown = 40; 
    
    this.yInicial = this.posicio.y; 
    this.angle = 0;
    this.posicioJugador = posicioJugador;
  }

  public void actualitzar() {
    super.actualitzar();
    
    // Moviment ondulat (sinusoide)
    this.angle += 0.03f; 
    this.posicio.y = this.yInicial + (float)Math.sin(this.angle) * 30; 
    
    this.temporizador++;
  }

  public void mostrar(PApplet app) {
    app.fill(255, 0, 255);
    app.noStroke();
    app.ellipse(this.posicio.x, this.posicio.y, this.tamany, this.tamany);
    
    // Dibuixem la barra de vida de 30 HP màxims
    super.dibuixarBarraVida(app, 30);
  }

  public Dispar disparar() {
    if (this.temporizador >= this.cooldown) {
      this.temporizador = 0;
      if (this.posicioJugador != null) {
        return new Dispar(this.posicio, this.posicioJugador, true); 
      }
      return new Dispar(this.posicio, true); 
    }
    return null;
  }
}
