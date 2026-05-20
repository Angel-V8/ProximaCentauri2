import processing.core.*;

/**
* Simula animacions basant-se en una seqüència de frames que s'extraeun bé d'una carpeta o bé d'un spritesheet. 
* Aquesta classe contempla tres tipus d'animacions: automàtiques en bucle, automàtiques sense bucle i no automàtiques
* - Les animacions automàtiques en bucle són aquelles que s'inicien automàticament i es repeteixen en bucle sense parar
* - Les animacions automàtiques sense bucle són aquelles que s'inicien automàticament però tenen inici i final
* - Les animaciones no automàtiques són aquelles que requereixen de l'interacció amb l'usuari per iniciar-se i parar-se
* @author Vicent Sanchis
* @since  14 de març de 2022
*/
public class Animation {

  protected PApplet app;              /** Referència al PApplet principal per a poder pintar */

  protected boolean   idle;           /** Indica si l'animació està parada (true) o executant-se (false) */
  protected PImage [] images;         /** Array d'imatges que formen l'animació */
  protected int       totalImages;    /** Total d'imatges (o frames) que formen l'animació */     
  protected int       initialFrame;   /** Frame inicial de l'animació quan aquesta comença */
  protected int       currentFrame;   /** Frame actual que està mostrant-se de l'animació */ 
  protected int       frameDelay;     /** Retras entre canvis de frames. A major delay més lenta és l'animació */
  protected int       currentDelay;   /** Total del delay que s'ha complert ja abans de canviar de frame */
  protected boolean   loop;           /** Indica si l'animació és un bucle infinit (true) o si només es mostra una vegada i para (false) */
  protected boolean   finished;       /** Indica si l'animació ha acabat */
  protected boolean   isDynamic;      /** Si una animació és dinàmica canvia de frames de forma automàtica */
  protected String    tag;            /** Etiqueta per identificar l'animació */

  /** * Construeix un objecte buit
  */
  public Animation ()  { }

  /**
  * Constructor de la classe Animation, se li passa la ruta i el prefix de les imatges (frames) que formaran l'animació.
  * Amb aquest constructor el nom de les imatges haurien de tindre el següent format prefix0000.png (0000, 0001, 0002...)
  * @param app referència al PApplet principal
  * @param imgPrefix prefix de cadascuna de les imatges que formaran l'animació
  * @param total nombre total de frames que formaran l'animació
  * @param frameWait nombre de frames que ha d'esperar entre canvi i canvi a l'animació.
  * @param bLoop indica si és una animació que s'executa en bucle infinit o no.
  */
  public Animation (PApplet app, String strTag, String strPath, String imgPrefix, int total) {
    this.app = app; 
    this.totalImages  = total;
    this.images       = new PImage [totalImages];
    this.currentDelay = 0;
    this.initialFrame = 0;
    this.currentFrame = 0;
    this.frameDelay   = 5;
    this.loop         = true;
    this.finished     = false;
    this.isDynamic    = true;
    this.tag          = strTag;
    
    // Carreguem les imatges a l'array d'imatges
    for (int i=0; i < totalImages; i ++ ) {
      // Nota: nf() és una funció de PApplet, per això fem app.nf()
      String strFileName = imgPrefix + app.nf(i,4) + ".png";
      try {
        images[i] = app.loadImage(strPath + strFileName);
      }
      catch (Exception e) {
        e.printStackTrace();
        return;
      }
    }
  }

  /**
  * Constructor de la classe Animation a partir d'un sprite sheet.
  * A diferència del constructor anterior on aquest es creava utlitzant imatges individuals, aquest constructor utilitza un spriteSheet on
  * estan totes les imatges que formen l'animació en forma de graella, per tant serà necessari indicar el número de files, columnes i l'alt i ample 
  * de cadascun dels frames (han de tindre tots les mateixes dimensions).
  * @param app referència al PApplet principal
  * @param strTag etiqueta per a les imatges del sprite
  * @param strSheetPath ruta de l'sprite
  * @param w ample de cada imatge del sprite (No de tot l'sprite)
  * @param h alt de cada imatge del sprite
  * @param files total files del nostre sprite
  * @param cols total columnes del sprite
  * @param quinaFila si el valor és zero agafara totes les imatges com una unica seqüència si quinaFila és major que zero agafarà només una fila
  */
  public Animation (PApplet app, String strTag, String strSheetPath, int w, int h, int files, int cols, int quinaFila) {
    this.app = app;
    // Inicialitzem valors
    if (quinaFila == 0)
      totalImages  = files*cols;
      
    else
      totalImages = cols;
      
    currentDelay = 0;
    initialFrame = 0;
    currentFrame = 0;
    frameDelay   = 5;
    loop         = true;
    isDynamic    = true;
    finished     = false;
    images = new PImage[totalImages];
    tag    = strTag;
    
    // Carreguem els fotogrames de l'sprite
    PImage imgSheet;  
    try {
      imgSheet = app.loadImage(strSheetPath);
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }
    
    if (quinaFila == 0) {
      for(int i=0; i < files; i ++) {
        for(int j=0; j < cols; j ++) {
          int x = j*w;
          int y = i*h;
          int cont = (i*cols)+j;
          if (imgSheet != null) {
             images[cont] = imgSheet.get(x,y,w,h);
          }
        }
      }
    }
    else {
      for (int i=0; i < cols; i++) {
        int x = i*w;
        int y = (quinaFila-1)*h;
        if (imgSheet != null) {
           images[i] = imgSheet.get(x,y,w,h);
        }
      }
    }
  }

  /**
  * Constructor de còpia de l'animació.
  * @return una còpia de l'objecte animació
  */
  public Animation copy() {
    Animation newAnim = new Animation();
    
    newAnim.app          = this.app; // Copiem la referència a l'app
    newAnim.images       = this.images;
    newAnim.totalImages  = this.totalImages;
    newAnim.initialFrame = this.initialFrame;
    newAnim.currentFrame = this.currentFrame;
    newAnim.frameDelay   = this.frameDelay;
    newAnim.currentDelay = this.currentDelay;
    newAnim.loop         = this.loop;
    newAnim.finished     = this.finished;
    newAnim.isDynamic    = this.isDynamic;
    newAnim.tag          = this.tag;
    
    return newAnim;
  }

  /**
  * Mostra la imatge o frame actual a la posició 'loc' i en la direcció 'dir'
  * @param loc PVector que ens indica les coordenades on s'ha de dibuixar la imatge
  * @param dir en quina direcció horitzontal (dreta o esquerra) s'ha de mostrar la imatge
  */
  public void display(PVector loc, int dir, float scale) {
    if (app == null) return; 

    app.pushMatrix();
      PImage imgShow;
      app.imageMode(PConstants.CENTER);
      
      if (this.idle && images.length > 1)
        imgShow = images[1];
        
      else if (images.length > 0)
        imgShow = images[currentFrame];
      else
        return;
        
      if (imgShow != null) {
        if (scale == 1 )
          app.image(imgShow, dir*loc.x, loc.y);
          
        else
          app.image(imgShow, dir*loc.x, loc.y, imgShow.height*scale, imgShow.width*scale);
      }
        
      app.scale(dir, 1);
      
    app.popMatrix();
  }

  /**
  * Actualitza l'animació, bàsicament prepara l'estat per a mostrar el pròxim fotograma
  * Hi han tres tipus d'animacions:
  * 1. Animacions dinàmiques i en bucle: són animacions que quan comencen es reprodueixen en bucle sense parar. Exemple: idle 
  * 2. Animacions dinàmiques sense bucle: són animacions que comencen i es reprodueixen de forma automàtica pero quan acaben paren. Exemple: explosió, mort d'un personatge.
  * 3. Animacions estàtiques són les que depenen de l'acció de l'usuari per a que s'activen. Exemple: moure una nau a un costat o altre
  */
  public void update() {
    // Si l'animació no és dinàmica no ha d'actualitzar res (Ex: Animació tilt)
    if (!isDynamic)
      return;
      
    // Si l'animació és en BUCLE quan arribem a l'últim frame haurem de començar de zero
    if (loop) {
      if (currentDelay == 0) 
        currentFrame = (currentFrame+1)%totalImages;
      
      if (frameDelay > 0)
        currentDelay = (currentDelay + 1)%frameDelay;
    }
    
    // Si pel contrari és una animació que no es repeteix infinitament, quan acabe ho hem d'indicar 
    else {
      if(currentDelay == 0) {
        currentFrame ++;
        if (currentFrame >= totalImages) {
          currentFrame = totalImages - 1;
          finished = true;
        }
      }
      currentDelay = (currentDelay+1)%frameDelay;
    }
  }

  /**
  * Mètode només aplicable a les animacions no dinàmiques
  * Gira l'animació des del frame actual cap als frames situats a l'esquerra de l'animació
  */
  public void tiltLeft () {
    if (currentFrame > 0)
      currentFrame --;
  }

  /**
  * Mètode només aplicable a les animacions no dinàmiques
  * Gira l'animació des del frame actual cap als frames situats a la dreta de l'animació
  */
  public void tiltRight () {
    if (currentFrame < totalImages-1)
      currentFrame ++;
  }

  /**
  * Mètode només aplicable a les animacions no dinàmiques
  * Fa tornar l'animació al seu frame central.
  */
  public void tiltBack () {
    if (currentFrame < initialFrame)
      currentFrame ++;
    
    else if (currentFrame > initialFrame)
      currentFrame --;
  }

  /** * Mètode que reinicia les animacions que no són bucles
  */
  public void reset () {
    currentFrame = initialFrame;
    currentDelay = 0;
    finished     = false;
  }

  /** * Per defecte una animació el seu frame inicial serà el 0 però es pot donar el cas que necessitem que comence per una altre frame
  * @param iFrame enter que determina quin serà el primer frame de l'animació 
  */
  public void setInitialFrame (int iFrame) {
    if (iFrame >= totalImages || iFrame < 0)
      return;
      
    else
      initialFrame = iFrame;
  }

  /** * Estableix quin és el frame que s'està mostrant en l'actualitat.
  * @param iFrame enter que determina quin serà el frame actual 
  */
  public void setCurrentFrame (int iFrame) {
    if (iFrame >= totalImages || iFrame < 0)
      return;
      
    else
      currentFrame = iFrame;
  }

  /**
  * Mètode que ens mostra quina és la imatge inicial de l'animació
  * @return PImage image inicial de l'animació
  */
  public PImage getInitialFrame() {
    return this.images[initialFrame];
  }

  /**
  * Aquest mètode retorna quin és el frame actual de l'animació
  * @return enter que representa el frame actual de l'animació
  */
  public int getCurrentFrame () {
    return currentFrame;
  }

  /**
  * Obté el delay entre canvi de frames 
  * @return int total de frames que espera fins a poder realitzar el canvi
  */
  public int getDelay () {
    return this.frameDelay;
  }

  /**
  * setDelay estableix el nombre de frames que espera entre fotograma i fotograma
  * A major delay més lenta serà l'animació
  * @param delay enter que determina el total de frames a esperar.
  */
  public void setDelay (int delay) {
    frameDelay = delay;
  }

  /**
  * Indica si l'animació és dinàmica o no
  * @return boolean true si és dinàmica i false si no ho és
  */
  public boolean isDynamic () {
    return isDynamic;
  }

  /**
  * Estableix l'animació com una animació dinàmica o no depenent del valor que es passe per paràmetre
  * @param value valor que se li vol donar: true = dinamica, false = no dinàmica.
  */
  public void setDynamic(boolean value) {
    isDynamic = value;
  }

  /**
  * Indica si l'animació a banda de dinàmica és en bucle
  * @return true si l'animació es bucle false en cas contrari.
  */
  public boolean isLoop () {
    return loop;
  }

  /**
  * setLoop determina si l'animació és un bucle o s'inicia i acaba
  * @param b valor booleà que determina si es bucle o no
  */
  public void setLoop (boolean b) {
    loop = b;
  }

  /**
  * Mètode que ens diu si l'animació (que no és loop) ha acabat
  */  
  public boolean hasFinished() {
    if (loop) 
      return false;
      
    return finished;
  }

  /**
  * Estableix l'etiqueta de l'animació 
  * @param tag etiqueta que se li aplicarà a l'animació
  */
  public void setTag (String strTag) {
    this.tag = strTag;
  }

  /**
  * Obté l'etiqueta de l'animació actual
  * @return String l'etiqueta de l'animació
  */
  public String getTag() {
    return tag;
  }
  
  public void setIdle (boolean valor) {
    this.idle = valor;
  }
}
