import processing.core.PApplet;
import processing.core.PVector;

public class Mina {
  // ATRIBUTS
  private PVector posicio;
  private float velocitatX;
  private float velocitatY;
  private int tamany;
  private boolean actiu;
  
  // NOUS ATRIBUTS PER A EXPLOSIÓ I VIDA
  private int vida;
  private boolean explotant;
  private int radiExplosio;
  private boolean haDanyatJugador; // Evita danyar repetidament al jugador durant l'explosió
  private Animation animacio; // Animació del parpadeig de la mina
  private Animation animacioExplosio; // Animació de l'explosió de la mina

  // METODES
  public void actualitzar() {
    if (explotant) {
      radiExplosio += 5; // L'explosió es fa gran (física de dany expansiu)
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      // Frena a la quarta part de la seua velocitat
      this.posicio.x -= velocitatX * 0.25f;
      this.posicio.y += velocitatY * 0.25f;
    } else {
      this.posicio.x -= velocitatX;
      this.posicio.y += velocitatY; 
      
      // Evitem que isquen per dalt i per baix fent-les rebotar!
      if (this.posicio.y < 30 || this.posicio.y > 570) {
        this.velocitatY *= -1; // Invertim la direcció vertical
      }

      if (this.animacio != null) {
        this.animacio.update();
      }
    }
  }

  public void mostrar(PApplet app) {
    if (explotant) {
      if (this.animacioExplosio == null) {
        // Spritesheet de 1024x1024 en quadrícula 4x4 -> 16 frames de 256x256
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 256, 256, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(2);
      }
      this.animacioExplosio.display(this.posicio, 1, (float)this.tamany / 128.0f);
    } else {
      if (this.animacio == null) {
        // Spritesheet de 1024x1024 en quadrícula 2x2 -> 4 frames de 512x512
        this.animacio = new Animation(app, "Mina", "./img/mina.png", 512, 512, 2, 2, 0);
        this.animacio.setLoop(true);
        this.animacio.setDelay(8); // Velocitat de parpadeig de la mina
      }
      this.animacio.display(this.posicio, 1, (float)this.tamany / 512.0f);
    }
  }

  // CONSTRUCTORS

  // Constructor per defecte
  public Mina() {
    this.posicio = new PVector(0, 0);
    
    // Xicoteta velocitat aleatòria per a que deriven lentament com si suraren
    this.velocitatX = (float)Math.random() * 1.5f - 0.2f; 
    this.velocitatY = (float)Math.random() * 1.5f - 0.75f; 
    this.tamany = 50; // Mida original de la mina
    this.actiu = true;
    this.vida = 20; // Calen 2 tirs per a detonar-la
    this.explotant = false;
    this.radiExplosio = this.tamany;
    this.haDanyatJugador = false;
  }

  // Constructor parametritzat
  public Mina(PVector origen) {
    this();
    // Copiem la posicio de qui dispara nau o enemic
    this.posicio = origen.copy();
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public int getTamany() {
    return this.tamany;
  }
  
  // NOUS MÈTODES
  public void rebreDany(int dany) {
    if (explotant) return;
    this.vida -= dany;
    if (this.vida <= 0) detonar();
  }
  
  public void detonar() { this.explotant = true; }
  
  public boolean isExplotant() { return this.explotant; }
  
  public boolean haAcabatExplosio() { return this.explotant && this.animacioExplosio != null && this.animacioExplosio.hasFinished(); }

  public int getRadiExplosio() { return this.radiExplosio; }

  public boolean haDanyatJugador() { return this.haDanyatJugador; }

  public void setHaDanyatJugador(boolean b) { this.haDanyatJugador = b; }
}
