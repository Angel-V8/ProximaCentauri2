import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class NauPlayer implements Entitat {

  // ATRIBUTS
  private PVector posicio;
  private PVector velocitat;
  private int vida;
  private boolean viu;
  private int tamany;
  private int escut;
  private ArrayList<Dispar> disparos;

  // ATRIBUTS ANIMACIÓ
  private Animation anim;

  // Interruptors moviment
  private boolean amunt, avall, esquerra, dreta;
  private boolean disparant;

  // Timer de dispar (reduït per disparar més ràpid)
  private int cooldown = 12;
  private int timer = 12;
  private int tempsDobleDispar = 0; // Temporitzador del BoosterAtac

  // CONSTRUCTOR
  public NauPlayer(PApplet app) {
    this.posicio = new PVector(100, 300);
    this.velocitat = new PVector(5, 5);
    this.vida = 100;
    this.viu = true;
    this.tamany = 46;
    this.escut = 0;

    this.anim = new Animation(app, "Nau", "./img/Sprites nave.png", 46, 34, 1, 8, 0);
    this.anim.setDynamic(false);
    this.disparos = new ArrayList<Dispar>();
  }

  // METODES MOVIMENT
  public void setMoureAmunt(boolean b) {
    this.amunt = b;
  }
  public void setMoureAvall(boolean b) {
    this.avall = b;
  }
  public void setMoureEsquerra(boolean b) {
    this.esquerra = b;
  }
  public void setMoureDreta(boolean b) {
    this.dreta = b;
  }
  
  public void setDisparant(boolean b) {
    this.disparant = b;
  }

  public void actualitzar() {
    // Logica de Moviment
    if (this.amunt)    this.posicio.y -= this.velocitat.y;
    if (this.avall)    this.posicio.y += this.velocitat.y;
    if (this.esquerra) this.posicio.x -= this.velocitat.x;
    if (this.dreta)    this.posicio.x += this.velocitat.x;

    // Limits pantalla
    if (this.posicio.x < 20) this.posicio.x = 20;
    if (this.posicio.x > 800 - 20) this.posicio.x = 800 - 20;
    if (this.posicio.y < 20) this.posicio.y = 20;
    if (this.posicio.y > 600 - 20) this.posicio.y = 600 - 20;

    this.timer++;
    if (this.tempsDobleDispar > 0) this.tempsDobleDispar--;
      // Dispar continu (foc automàtic) si la tecla d'espai/x està premuda
    if (this.disparant) {
      this.dispar();
    }

  
    if (this.amunt) {
      // Si anem amunt, posem el frame 3 (inclinat dalt)
      this.anim.setCurrentFrame(3);
    } else if (this.avall) {
      // Si anem avall, posem el frame 4 (inclinat baix)
      this.anim.setCurrentFrame(4);
    } else {
      // Si estem quiets o esquerra/dreta, frame 0 (normal)
      this.anim.setCurrentFrame(0);
    }

    // Moure bales del jugador
    for (int i = this.disparos.size() - 1; i >= 0; i--) {
      Dispar d = this.disparos.get(i);
      d.actualitzar();
      if (d.getPosicio().x > 800) {
        this.disparos.remove(i);
      }
    }
  }

  public void mostrar(PApplet app) {
    // Dibuixem l'animacio en lloc de la boleta
    this.anim.display(this.posicio, 1, 1.0f);

    for (Dispar d : this.disparos) {
      d.mostrar(app);
    }

    // Dibuixem una bombolla visual si tenim l'escut actiu
    if (this.escut > 0) {
      app.pushStyle();
      app.noFill();
      app.stroke(0, 200, 255, 180); // Blau elèctric
      app.strokeWeight(3);
      app.ellipse(this.posicio.x, this.posicio.y, this.tamany + 15, this.tamany + 15);
      app.popStyle();
    }
  }

  // Implementacio Destruible
  public void rebreDany(int dany) {
    if (this.escut > 0) {
      this.escut--; // L'escut absorbeix tot el colp
    } else {
      this.vida -= dany;
    }
  }

  public boolean estaDestruit() {
    return (this.vida <= 0);
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public void dispar() {
    if (this.timer >= this.cooldown) {
      this.timer = 0;
      if (this.tempsDobleDispar > 0) {
        PVector pos1 = this.posicio.copy(); pos1.y -= 10;
        PVector pos2 = this.posicio.copy(); pos2.y += 10;
        this.disparos.add(new Dispar(pos1, false));
        this.disparos.add(new Dispar(pos2, false));
      } else {
        this.disparos.add(new Dispar(this.posicio.copy(), false));
      }
    }
  }

  // METODE DE COLISIONS
  public boolean colisionaAmb(Object obj) {

    PVector pos2 = null;
    int tam2 = 0;

    // VEgem què és l'objecte
    if (obj instanceof Enemic) {
      Enemic e = (Enemic) obj;
      if (e.isDestruint()) return false;
      pos2 = e.getPosicio();
      tam2 = e.getTamany();
    } else if (obj instanceof Meteorit) {
      Meteorit m = (Meteorit) obj;
      if (m.isDestruint()) return false;
      pos2 = m.getPosicio();
      tam2 = m.getTamany();
    } else if (obj instanceof Mina) {
      Mina mi = (Mina) obj;
      pos2 = mi.getPosicio();
      tam2 = mi.getTamany();
    } else if (obj instanceof Booster) {
      Booster b = (Booster) obj;
      pos2 = b.getPosicio();
      tam2 = b.getTamany();
    } else if (obj instanceof Dispar) {
      Dispar d = (Dispar) obj;
      pos2 = d.getPosicio();
      tam2 = d.getTamany();
    }

    // Si hem reconegut l'objecte, comprovem la col·lisió amb Utils
    if (pos2 != null) {
      return Utils.hiHaColisio(this.posicio, this.tamany, pos2, tam2);
    }

    return false; // Si no és res d'això, no hi ha col·lisió
  }

  public ArrayList<Dispar> getDisparos() {
    return this.disparos;
  }

  public void aplicarConfiguracio(int novaVida, int novaVel, int nouEscut) {
    this.vida = novaVida;
    this.velocitat = new PVector(novaVel, novaVel);
    this.escut = nouEscut;
  }
  
  public void curar(int quantitat) {
    this.vida += quantitat;
    if (this.vida > 100) this.vida = 100;
  }
  
  public void afegirEscut() {
    this.escut = 1;
  }
  
  public void activarDobleDispar(int frames) {
    this.tempsDobleDispar = frames; 
  }

  public void resetJugador() {
    this.posicio = new PVector(100, 300);
    this.disparos.clear();
    this.viu = true;
    this.disparant = false;
    this.timer = this.cooldown; // Per poder disparar només començar
  }
  
  public int getVida() {
        return this.vida;
    }

  public int getTamany() {
        return this.tamany;
    }

}
