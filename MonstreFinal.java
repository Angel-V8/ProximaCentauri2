import processing.core.PApplet;
import processing.core.PVector;

public class MonstreFinal {

  // ATRIBUTS
  private PVector posicio;
  private int vida;
  private int vidaMaxima;
  private int ample;
  private int alt;
  private boolean actiu;

  // ATRIBUTS DE COMBAT I MOVIMENT
  private int temporizadorDispar;
  private int cooldownDispar;
  private boolean destruint;
  private Animation animacioExplosio;

  // MOVIMENT NATURAL
  private PVector targetPosicio;
  private int temporizadorNouTarget;

  // GESTIÓ D'ATACS RÀPIDS (Ràfegues)
  private int rafagaRestant;
  private int temporizadorRafaga;

  // MECÀNICA DEL KAMEHAMEHA
  private boolean cargantKamehameha;
  private boolean disparantKamehameha;
  private int temporizadorKamehameha;
  private float kamehamehaY;

  // ANIMACIÓ DE GOKU I VECTOR ZERO
  private Animation animacio;
  private PVector posicioZero; // PVector(0,0) per a renderitzar amb translat/scale in-place

  private boolean esDificil = true; // NOU: Control de dificultat del boss

  // CONSTRUCTORS

  // Constructor per defecte
  public MonstreFinal() {
    this.posicio = new PVector(900, 300); // Neix fora de pantalla a la dreta
    this.vida = 1600; // Salut inicial doblada (1600)
    this.vidaMaxima = 1600;
    this.ample = 120; // Mida de Goku en pantalla (120 píxels)
    this.alt = 120;
    this.actiu = true;
    
    this.temporizadorDispar = 0;
    this.cooldownDispar = 60; // Ataca cada 1 segon
    this.destruint = false;

    this.targetPosicio = new PVector(620, 300);
    this.temporizadorNouTarget = 0;

    this.rafagaRestant = 0;
    this.temporizadorRafaga = 0;

    this.cargantKamehameha = false;
    this.disparantKamehameha = false;
    this.temporizadorKamehameha = 0;
    this.kamehamehaY = 300.0f;

    this.posicioZero = new PVector(0, 0);
  }

  // Constructor parametritzat (motius de compatibilitat: esDificil = true per defecte)
  public MonstreFinal(int nivell) {
    this(nivell, true);
  }

  // NOU: Constructor parametritzat que rep la dificultat per a escalar estadístiques
  public MonstreFinal(int nivell, boolean esDificil) {
    this();
    this.esDificil = esDificil;
    if (esDificil) {
      this.vida = 1600 + (nivell - 10) * 400; // Dificil: 1600 HP base
      this.cooldownDispar = 32; // Cooldown d'atac de ~0.53s (32 frames)
    } else {
      this.vida = 800 + (nivell - 10) * 200;  // Normal: 800 HP base
      this.cooldownDispar = 50; // Cooldown d'atac de ~0.83s (50 frames)
    }
    this.vidaMaxima = this.vida;
  }

  // METODE AUXILIAR LERP (Per evitar dependència de PApplet fora del render)
  private float lerp(float start, float stop, float amt) {
    return start + (stop - start) * amt;
  }

  // METODES
  public void actualitzar() {
    if (this.destruint) {
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      this.posicio.x += 0.25f;
      return;
    }

    if (!this.actiu) return;

    if (this.animacio != null) {
      this.animacio.update();
    }

    // 1. GESTIÓ DE LA CÀRREGA DEL KAMEHAMEHA (Goku es queda quiet levitant)
    if (this.cargantKamehameha) {
      this.temporizadorKamehameha++;
      if (this.temporizadorKamehameha >= 70) { // 70 frames de càrrega (~1.1 segons)
        this.cargantKamehameha = false;
        this.disparantKamehameha = true;
        this.temporizadorKamehameha = 0;
      }
      return;
    }

    // 2. GESTIÓ DEL DISPAR DEL KAMEHAMEHA (Goku manté la posició)
    if (this.disparantKamehameha) {
      this.temporizadorKamehameha++;
      if (this.temporizadorKamehameha >= 45) { // 45 frames de raig actiu (~0.75 segons)
        this.disparantKamehameha = false;
        this.temporizadorKamehameha = 0;
      }
      return;
    }

    // 3. MOVIMENT DE PRESENTACIÓ: Avança cap a la seua posició fins x = 620
    if (this.posicio.x > 620) {
      this.posicio.x -= 2.0f;
    } else {
      // 4. MOVIMENT DE COMBAT FLUID: El cap tria un punt de destinació aleatori i hi vola
      this.temporizadorNouTarget++;
      if (this.temporizadorNouTarget >= 120) { // Canvia de rumb cada 2 segons
        this.temporizadorNouTarget = 0;
        this.targetPosicio.x = (float)(Math.random() * 150) + 530; // X entre 530 i 680
        this.targetPosicio.y = (float)(Math.random() * 360) + 120; // Y entre 120 i 480
      }
      
      this.posicio.x = lerp(this.posicio.x, this.targetPosicio.x, 0.025f);
      this.posicio.y = lerp(this.posicio.y, this.targetPosicio.y, 0.025f);
    }
  }

  public void mostrar(PApplet app) {
    if (!this.actiu) return;

    if (this.destruint) {
      if (this.animacioExplosio == null) {
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 64, 64, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(4);
      }
      this.animacioExplosio.display(this.posicio, 1, 4.5f);
      return;
    }

    // Inicialització de l'animació de Goku
    if (this.animacio == null) {
      this.animacio = new Animation(app, "Goku", "./img/goku.png", 64, 64, 4, 4, 1);
      this.animacio.setLoop(true);
      this.animacio.setDelay(8);
    }

    // Dibuix dels efectes especials del Kamehameha abans del personatge
    if (this.cargantKamehameha) {
      // A) Corredor de perill vermell translúcid (mida del futur làser) i línia guia
      app.pushStyle();
      app.rectMode(PApplet.CORNERS);
      app.noStroke();
      
      // Si falten menys de 15 frames, ja està blocat: parpelleja agressivament en vermell/taronja brillant
      boolean blocat = (this.temporizadorKamehameha >= 55);
      if (blocat) {
        float warningAlpha = 40 + (app.frameCount % 5) * 25; // Parpelleig molt ràpid i intens
        app.fill(255, 50, 0, warningAlpha);
        app.rect(0, this.kamehamehaY - 50, this.posicio.x - 40, this.kamehamehaY + 50);
        
        app.stroke(255, 200, 0, 220); // Línia groga de bloqueig
        app.strokeWeight(4);
      } else {
        float warningAlpha = 15 + (app.frameCount % 10) * 6; // Parpelleig suau de guia
        app.fill(255, 0, 0, warningAlpha);
        app.rect(0, this.kamehamehaY - 50, this.posicio.x - 40, this.kamehamehaY + 50);
        
        app.stroke(255, 0, 0, 150); // Línia vermella de seguiment
        app.strokeWeight(2);
      }
      app.line(this.posicio.x - 40, this.posicio.y, 0, this.kamehamehaY);
      app.popStyle();

      // B) Bola d'energia blava creixent a les seues mans
      float midaBola = PApplet.map(this.temporizadorKamehameha, 0, 70, 15, 100);
      app.pushStyle();
      app.noStroke();
      app.fill(0, 220, 255, 180 + (app.frameCount % 5) * 15);
      app.ellipse(this.posicio.x - 40, this.posicio.y, midaBola, midaBola);
      app.fill(255, 255, 255, 230);
      app.ellipse(this.posicio.x - 40, this.posicio.y, midaBola * 0.5f, midaBola * 0.5f);
      app.popStyle();
    } 
    
    if (this.disparantKamehameha) {
      // C) Raig de làser massiu (Tres capes de color per a efecte neó DBZ - Amplada de 100px total)
      app.pushStyle();
      app.rectMode(PApplet.CORNERS);
      app.noStroke();
      
      // Capa 1: Aura blava resplendent exterior (100px amplada)
      app.fill(0, 180, 255, 130 + (app.frameCount % 4) * 30);
      app.rect(0, this.kamehamehaY - 50, this.posicio.x - 40, this.kamehamehaY + 50);
      
      // Capa 2: Cos de feix cian (60px amplada)
      app.fill(0, 240, 255, 220);
      app.rect(0, this.kamehamehaY - 30, this.posicio.x - 40, this.kamehamehaY + 30);
      
      // Capa 3: Nucli blanc pur desintegrador (30px amplada)
      app.fill(255, 255, 255, 255);
      app.rect(0, this.kamehamehaY - 15, this.posicio.x - 40, this.kamehamehaY + 15);
      
      app.popStyle();

      // D) Esfera de descàrrega a les mans de Goku
      app.pushStyle();
      app.noStroke();
      app.fill(0, 220, 255, 200 + (app.frameCount % 3) * 20);
      app.ellipse(this.posicio.x - 40, this.posicio.y, 130, 130);
      app.fill(255);
      app.ellipse(this.posicio.x - 40, this.posicio.y, 70, 70);
      app.popStyle();
    }

    // Render de Goku girat mirant a l'esquerra
    float hoverY = (float)Math.sin(app.frameCount * 0.08f) * 6.0f;
    app.pushMatrix();
    app.translate(this.posicio.x, this.posicio.y + hoverY);
    app.scale(-1, 1);
    float escala = (float)this.ample / 64.0f;
    this.animacio.display(this.posicioZero, 1, escala);
    app.popMatrix();

    // -------------------------------------------------------------
    // DIBUIX DE LA BARRA DE VIDA DEL JEFE (HUD INFERIOR PANTALLA)
    // -------------------------------------------------------------
    app.pushStyle();
    
    // Contenidor HUD
    app.fill(0, 0, 10, 180);
    app.noStroke();
    app.rectMode(PApplet.CENTER);
    app.rect(app.width / 2, app.height - 40, 520, 34, 10);
    
    // Fons interior vermell
    app.rectMode(PApplet.CORNER);
    app.fill(60, 10, 10);
    app.rect(app.width / 2 - 250, app.height - 50, 500, 20, 5);
    
    // Barra activa
    app.fill(255, 20, 60);
    float ampleVida = PApplet.map(Math.max(0, this.vida), 0, this.vidaMaxima, 0, 500);
    app.rect(app.width / 2 - 250, app.height - 50, ampleVida, 20, 5);
    
    // Etiqueta del Boss
    app.fill(255);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.textSize(13);
    app.text("PROXIMA CENTAURI GUARDIAN (GOKU BOSS)", app.width / 2, app.height - 41);
    
    app.popStyle();
  }

  // Lógica per llançar atacs al jugador
  public java.util.ArrayList<Dispar> disparar(PVector posicioJugador) {
    java.util.ArrayList<Dispar> nousDispars = new java.util.ArrayList<Dispar>();
    if (this.destruint || !this.actiu) return nousDispars;

    // Només ataca si ja s'ha col·locat en posició de combat
    if (this.posicio.x <= 620) {
      
      // Si està carregant, orienta la Y del làser cap a la Y actual del jugador (fins al frame 65 per poder esquivar-lo al final)
      if (this.cargantKamehameha) {
        if (this.temporizadorKamehameha < 65) {
          this.kamehamehaY = lerp(this.kamehamehaY, posicioJugador.y, 0.28f); // Seguiment molt més ràpid i agressiu
        }
        return nousDispars;
      }

      // Si ja està disparant el feix massiu, no fa altres accions, pero el laser segueix lentament al jugador
      if (this.disparantKamehameha) {
        this.kamehamehaY = lerp(this.kamehamehaY, posicioJugador.y, 0.05f); // Es mou un poc cap al jugador durant el dispar!
        return nousDispars;
      }

      // PATRÓ 1: Ràfega activa de Ki Blasts
      if (this.rafagaRestant > 0) {
        this.temporizadorRafaga++;
        if (this.temporizadorRafaga >= 5) {
          this.temporizadorRafaga = 0;
          this.rafagaRestant--;
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), posicioJugador, 8.5f, 10.0f, true));
        }
        return nousDispars;
      }

      this.temporizadorDispar++;
      if (this.temporizadorDispar >= this.cooldownDispar) {
        this.temporizadorDispar = 0;

        // Triem un dels 5 atacs possibles a l'atzar
        int tipusAtac = (int)(Math.random() * 5);

        if (tipusAtac == 0) {
          // ATAC 0: Ventall triple ràpid
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), -9.0f, 0, true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 20), -9.0f, -2.5f, true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 20), -9.0f, 2.5f, true));
        } 
        else if (tipusAtac == 1) {
          // ATAC 1: Doble blast dirigit ràpid i precís
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 25), posicioJugador, 8.5f, 8.0f, true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 25), posicioJugador, 8.5f, 8.0f, true));
        } 
        else if (tipusAtac == 2) {
          // ATAC 2: Ràfega seqüencial
          this.rafagaRestant = 5;
          this.temporizadorRafaga = 0;
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), posicioJugador, 8.5f, 10.0f, true));
        } 
        else if (tipusAtac == 3) {
          // ATAC 3: Ona Radial 360º de 10 ràpids blasts
          float anglePas = (float)(Math.PI * 2) / 10.0f;
          for (int i = 0; i < 10; i++) {
            float angle = i * anglePas;
            float vx = (float)Math.cos(angle) * 7.5f;
            float vy = (float)Math.sin(angle) * 7.5f;
            nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), vx, vy, true));
          }
        }
        else {
          // ATAC 4 (NOU): KAMEHAMEHA LASER DESINTEGRADOR
          this.cargantKamehameha = true;
          this.temporizadorKamehameha = 0;
          this.kamehamehaY = posicioJugador.y; // Fixa la Y inicial apuntant al jugador
        }
      }
    }
    return nousDispars;
  }

  public void rebreDany(int dany) {
    if (this.destruint) return;
    this.vida -= dany;
    if (this.vida <= 0) {
      this.vida = 0;
      this.destruint = true;
    }
  }

  public boolean estaDestruit() {
    return this.vida <= 0;
  }

  public boolean isDestruint() {
    return this.destruint;
  }

  public boolean haAcabatExplosio() {
    return this.destruint && this.animacioExplosio != null && this.animacioExplosio.hasFinished();
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public int getAmple() {
    return this.ample;
  }

  public int getAlt() {
    return this.alt;
  }

  public boolean isActiu() {
    return this.actiu;
  }

  public void setActiu(boolean b) {
    this.actiu = b;
  }

  // GETTERS PER AL KAMEHAMEHA
  public boolean isDisparantKamehameha() {
    return this.disparantKamehameha;
  }

  public float getKamehamehaY() {
    return this.kamehamehaY;
  }
}
