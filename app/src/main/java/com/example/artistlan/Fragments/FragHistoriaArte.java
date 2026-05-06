package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.HistoriaArte.adapter.HistoriaArteAdapter;
import com.example.artistlan.HistoriaArte.model.HistoriaArteItem;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;

import java.util.ArrayList;
import java.util.List;

public class FragHistoriaArte extends Fragment {

    private RecyclerView rvHistoriaArte;
    private ProgressBar pbHistoriaArte;
    private TextView tvEstadoHistoriaArte;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_frag_historia_arte, container, false);

        ThemeModuleStyler.styleFragment(this, root);

        rvHistoriaArte = root.findViewById(R.id.rvHistoriaArte);
        pbHistoriaArte = root.findViewById(R.id.pbHistoriaArte);
        tvEstadoHistoriaArte = root.findViewById(R.id.tvEstadoHistoriaArte);

        configurarRecycler();
        cargarContenidoLocal();

        return root;
    }

    private void configurarRecycler() {
        rvHistoriaArte.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistoriaArte.setHasFixedSize(false);
        rvHistoriaArte.setNestedScrollingEnabled(true);
        rvHistoriaArte.setItemAnimator(null);
    }

    private void cargarContenidoLocal() {
        pbHistoriaArte.setVisibility(View.VISIBLE);
        tvEstadoHistoriaArte.setVisibility(View.GONE);

        List<HistoriaArteItem> items = construirContenidoHistoriaArte();

        pbHistoriaArte.setVisibility(View.GONE);

        if (items.isEmpty()) {
            tvEstadoHistoriaArte.setVisibility(View.VISIBLE);
            tvEstadoHistoriaArte.setText("Aún no hay contenido disponible.");
            return;
        }

        rvHistoriaArte.setAdapter(new HistoriaArteAdapter(items));
    }

    private List<HistoriaArteItem> construirContenidoHistoriaArte() {
        List<HistoriaArteItem> lista = new ArrayList<>();

        lista.add(new HistoriaArteItem(
                "Introducción",
                "¿Qué es el arte?",
                "El arte es una forma de expresión humana que comunica ideas, emociones, creencias y formas de ver el mundo.",
                "El arte ha acompañado a la humanidad desde sus orígenes. A través de imágenes, objetos, sonidos, movimientos y espacios, las personas han representado sus miedos, deseos, dioses, historias, luchas y sueños.\n\n" +
                        "No existe una sola definición de arte. Puede ser belleza, protesta, memoria, identidad, técnica, juego, pensamiento o experimentación. Una obra artística puede buscar agradar, incomodar, enseñar, denunciar o simplemente provocar una experiencia."
        ));

        lista.add(new HistoriaArteItem(
                "Introducción",
                "Importancia del arte en la sociedad",
                "El arte ayuda a conservar memoria, construir identidad y expresar ideas que a veces no pueden decirse con palabras.",
                "El arte permite conocer cómo vivían, pensaban y sentían las personas de distintas épocas. Gracias a pinturas, esculturas, edificios, murales, fotografías y objetos artísticos podemos entender culturas antiguas y modernas.\n\n" +
                        "También tiene una función social. Puede denunciar injusticias, representar movimientos políticos, fortalecer comunidades, educar, decorar espacios o impulsar nuevas formas de imaginar el futuro."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte prehistórico",
                "Incluye pinturas rupestres, figuras rituales y representaciones de animales y escenas de caza.",
                "El arte prehistórico es una de las primeras formas conocidas de expresión humana. Aparece en cuevas, piedras, huesos y pequeños objetos. Las pinturas rupestres suelen mostrar animales, manos, símbolos y escenas relacionadas con la supervivencia.\n\n" +
                        "Ejemplos importantes son las cuevas de Altamira en España y Lascaux en Francia. Estas obras muestran que desde tiempos muy antiguos el ser humano buscaba representar su entorno y dejar huella de su existencia."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte egipcio",
                "Se caracteriza por su relación con la religión, la muerte, los faraones y la vida después de la muerte.",
                "El arte egipcio tenía una función religiosa y simbólica. Sus pinturas, esculturas y construcciones buscaban asegurar el orden, la eternidad y el vínculo entre los humanos y los dioses.\n\n" +
                        "Las figuras suelen verse de perfil, con proporciones jerárquicas: los personajes más importantes aparecen más grandes. Las pirámides, templos, sarcófagos y relieves son ejemplos de una cultura visual profundamente ligada al poder y la espiritualidad."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte griego",
                "Buscó la belleza ideal, la proporción, el equilibrio y la representación del cuerpo humano.",
                "El arte griego influyó enormemente en la historia occidental. Sus esculturas buscaron representar el cuerpo humano con armonía, movimiento y proporción. También desarrolló grandes avances en arquitectura, como los templos con columnas dóricas, jónicas y corintias.\n\n" +
                        "La idea de belleza clásica, equilibrio y perfección se convirtió en referencia para muchas épocas posteriores, especialmente durante el Renacimiento."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte romano",
                "Tomó influencias griegas, pero se enfocó más en el realismo, la ingeniería, los retratos y la propaganda política.",
                "El arte romano destacó por sus construcciones monumentales, como acueductos, anfiteatros, arcos de triunfo y termas. También desarrolló retratos realistas de emperadores, militares y ciudadanos importantes.\n\n" +
                        "Su arte servía para mostrar poder, orden y grandeza imperial. Obras arquitectónicas como el Coliseo reflejan la capacidad técnica y política de Roma."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte medieval",
                "Estuvo muy ligado a la religión cristiana y a la enseñanza visual de historias sagradas.",
                "Durante la Edad Media, gran parte del arte europeo se concentró en iglesias, monasterios y manuscritos iluminados. Las imágenes ayudaban a enseñar historias bíblicas a personas que no sabían leer.\n\n" +
                        "El arte medieval no siempre buscaba realismo, sino transmitir mensajes espirituales. Las figuras podían ser rígidas, frontales y simbólicas."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte románico",
                "Se caracteriza por iglesias robustas, arcos de medio punto, muros gruesos y esculturas religiosas.",
                "El arte románico se desarrolló principalmente entre los siglos XI y XII en Europa. Sus iglesias tenían apariencia sólida, con poca iluminación interior y decoración escultórica en portadas y capiteles.\n\n" +
                        "Las imágenes buscaban enseñar y advertir sobre temas religiosos como el juicio final, el pecado y la salvación."
        ));

        lista.add(new HistoriaArteItem(
                "Línea del tiempo",
                "Arte gótico",
                "Introdujo catedrales altas, vitrales luminosos, arcos apuntados y gran verticalidad.",
                "El gótico transformó la arquitectura medieval con catedrales más altas y luminosas. Los vitrales de colores llenaban los espacios de luz y narraban escenas religiosas.\n\n" +
                        "Ejemplos importantes son Notre Dame de París, la Catedral de Chartres y la Catedral de Colonia. Su estilo buscaba elevar la mirada y transmitir una sensación de cercanía con lo divino."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Renacimiento",
                "Fue una etapa de renovación artística inspirada en la antigüedad clásica, la ciencia y el humanismo.",
                "El Renacimiento surgió en Italia entre los siglos XIV y XVI. Los artistas comenzaron a estudiar anatomía, perspectiva, proporción y naturaleza. El ser humano se convirtió en un tema central.\n\n" +
                        "Artistas importantes: Leonardo da Vinci, Miguel Ángel, Rafael Sanzio y Sandro Botticelli.\n\n" +
                        "Obras destacadas: Mona Lisa, La última cena, La creación de Adán y El nacimiento de Venus."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Barroco",
                "Se caracteriza por el dramatismo, el movimiento, los contrastes de luz y sombra y la intensidad emocional.",
                "El Barroco se desarrolló entre los siglos XVII y XVIII. Sus obras buscan impactar al espectador con escenas teatrales, composiciones dinámicas y fuertes contrastes de claroscuro.\n\n" +
                        "Artistas importantes: Caravaggio, Bernini, Rembrandt, Rubens y Diego Velázquez.\n\n" +
                        "Obras destacadas: Las meninas, La vocación de San Mateo y La ronda de noche."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Neoclasicismo",
                "Retomó la estética clásica grecorromana, el orden, la razón y la sobriedad.",
                "El Neoclasicismo apareció en el siglo XVIII como reacción al exceso decorativo del Rococó. Se relacionó con ideas de razón, virtud cívica y modelos de la antigüedad.\n\n" +
                        "Jacques-Louis David fue uno de sus representantes más importantes. Sus obras muestran composiciones claras, figuras firmes y temas históricos o heroicos."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Romanticismo",
                "Valoró la emoción, la libertad, la naturaleza, lo sublime y la individualidad.",
                "El Romanticismo surgió a finales del siglo XVIII y se desarrolló durante el XIX. Se opuso a la frialdad racionalista y dio importancia al sentimiento, la imaginación y la fuerza de la naturaleza.\n\n" +
                        "Artistas importantes: Eugène Delacroix, Francisco de Goya, Caspar David Friedrich y William Turner.\n\n" +
                        "Obras destacadas: La libertad guiando al pueblo y El caminante sobre el mar de nubes."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Realismo",
                "Representó escenas de la vida cotidiana, trabajadores y problemas sociales sin idealizarlos.",
                "El Realismo surgió en el siglo XIX. Buscaba mostrar la realidad de forma directa, especialmente la vida de campesinos, obreros y personas comunes.\n\n" +
                        "Gustave Courbet y Jean-François Millet fueron figuras importantes. Este movimiento abrió camino para una mirada más social y crítica dentro del arte."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Impresionismo",
                "Buscó capturar la luz, el color y la impresión visual de un momento.",
                "El Impresionismo apareció en Francia en el siglo XIX. Sus artistas pintaban escenas al aire libre, usando pinceladas sueltas y colores luminosos.\n\n" +
                        "Artistas importantes: Claude Monet, Pierre-Auguste Renoir, Edgar Degas, Berthe Morisot y Camille Pissarro.\n\n" +
                        "Obra destacada: Impresión, sol naciente, de Monet, que dio nombre al movimiento."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Postimpresionismo",
                "Partió del Impresionismo, pero buscó mayor expresión, estructura o simbolismo.",
                "El Postimpresionismo no fue un solo estilo, sino una etapa donde varios artistas exploraron caminos personales después del Impresionismo.\n\n" +
                        "Van Gogh usó color intenso y pincelada expresiva; Cézanne buscó estructura y formas geométricas; Gauguin exploró simbolismo y color plano.\n\n" +
                        "Obras destacadas: La noche estrellada, Los girasoles y Los jugadores de cartas."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Expresionismo",
                "Deformó colores y formas para comunicar emociones intensas.",
                "El Expresionismo surgió a inicios del siglo XX. No buscaba representar la realidad de forma objetiva, sino expresar angustia, tensión, soledad o crítica social.\n\n" +
                        "Artistas importantes: Edvard Munch, Ernst Ludwig Kirchner, Egon Schiele y Wassily Kandinsky.\n\n" +
                        "Obra destacada: El grito, de Edvard Munch."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Cubismo",
                "Representó objetos y personas desde varios puntos de vista al mismo tiempo.",
                "El Cubismo fue desarrollado principalmente por Pablo Picasso y Georges Braque a inicios del siglo XX. Rompió con la perspectiva tradicional y fragmentó las formas en planos geométricos.\n\n" +
                        "Fue una revolución visual porque cambió la manera de representar el espacio, el volumen y el tiempo en una imagen.\n\n" +
                        "Obra destacada: Las señoritas de Avignon."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Surrealismo",
                "Exploró los sueños, el inconsciente, lo absurdo y las imágenes fantásticas.",
                "El Surrealismo se desarrolló en el siglo XX con influencia del psicoanálisis y las ideas sobre el inconsciente. Sus obras mezclan elementos reales de forma ilógica o sorprendente.\n\n" +
                        "Artistas importantes: Salvador Dalí, René Magritte, Max Ernst, Remedios Varo y Leonora Carrington.\n\n" +
                        "Obra destacada: La persistencia de la memoria."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Arte abstracto",
                "No representa objetos reconocibles, sino formas, colores, líneas y composiciones.",
                "El arte abstracto se aleja de la representación figurativa. Puede comunicar emociones, ritmo, energía o equilibrio mediante elementos visuales puros.\n\n" +
                        "Artistas importantes: Wassily Kandinsky, Piet Mondrian, Kazimir Malévich y Hilma af Klint.\n\n" +
                        "Obra destacada: Composición VIII."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Pop Art",
                "Usó imágenes de la cultura popular, publicidad, cómics y productos de consumo.",
                "El Pop Art surgió en los años cincuenta y sesenta. Cuestionó la separación entre arte culto y cultura popular.\n\n" +
                        "Artistas importantes: Andy Warhol, Roy Lichtenstein, Richard Hamilton y Claes Oldenburg.\n\n" +
                        "Obra destacada: Campbell’s Soup Cans."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Minimalismo",
                "Buscó reducir la obra a formas simples, materiales básicos y estructuras limpias.",
                "El Minimalismo se desarrolló en la segunda mitad del siglo XX. Eliminó lo decorativo y se enfocó en la presencia física del objeto, el espacio y la percepción.\n\n" +
                        "Usa geometría, repetición, materiales industriales y composiciones sobrias."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Arte conceptual",
                "Da más importancia a la idea que al objeto artístico.",
                "En el arte conceptual, la obra puede ser una acción, texto, documento, instrucción o proceso. Lo esencial es el concepto.\n\n" +
                        "Este movimiento cambió la idea tradicional de obra de arte y abrió el camino a instalaciones, performances y prácticas contemporáneas."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Arte contemporáneo",
                "Incluye prácticas actuales diversas: instalación, performance, video, arte digital, activismo y arte urbano.",
                "El arte contemporáneo no tiene un solo estilo. Puede abordar temas como identidad, tecnología, política, género, medio ambiente, memoria y globalización.\n\n" +
                        "Se caracteriza por la mezcla de técnicas y por cuestionar constantemente qué puede ser arte."
        ));

        lista.add(new HistoriaArteItem(
                "Movimientos",
                "Arte urbano",
                "Incluye grafiti, muralismo callejero, stencil, stickers e intervenciones en espacios públicos.",
                "El arte urbano utiliza la ciudad como soporte. Puede ser decorativo, político, identitario o crítico. En muchos casos busca dialogar directamente con la comunidad.\n\n" +
                        "Artistas como Banksy han llevado el street art a discusiones globales sobre espacio público, mercado del arte y protesta social."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Leonardo da Vinci",
                "Pintor, inventor y estudioso del Renacimiento, famoso por Mona Lisa y La última cena.",
                "Leonardo da Vinci fue una figura clave del Renacimiento italiano. Investigó anatomía, ingeniería, óptica, botánica y movimiento. Su arte combina observación científica, composición y misterio.\n\n" +
                        "Obras importantes: Mona Lisa, La última cena, La Virgen de las rocas.\n\n" +
                        "Su importancia está en unir arte y conocimiento, mostrando al artista como creador, investigador y pensador."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Miguel Ángel",
                "Escultor, pintor y arquitecto renacentista, conocido por David y la Capilla Sixtina.",
                "Miguel Ángel Buonarroti destacó por su dominio del cuerpo humano y la fuerza expresiva de sus figuras. Su escultura David es símbolo de ideal clásico y tensión psicológica.\n\n" +
                        "Obras importantes: David, La Piedad, La creación de Adán, Juicio Final."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Rafael Sanzio",
                "Maestro del equilibrio, la armonía y la composición durante el Renacimiento.",
                "Rafael fue reconocido por la claridad de sus composiciones y la belleza serena de sus figuras. Su obra representa uno de los puntos más altos del ideal renacentista.\n\n" +
                        "Obra destacada: La escuela de Atenas."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Caravaggio",
                "Pintor barroco famoso por su uso dramático del claroscuro.",
                "Caravaggio revolucionó la pintura con escenas intensas, personajes realistas y contrastes extremos de luz y sombra. Sus figuras parecen salir de la oscuridad, generando dramatismo inmediato.\n\n" +
                        "Obras importantes: La vocación de San Mateo, Judith decapitando a Holofernes."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Rembrandt",
                "Pintor neerlandés barroco, maestro del retrato, la luz y la introspección.",
                "Rembrandt exploró la condición humana con profundidad emocional. Sus autorretratos muestran el paso del tiempo, la fragilidad y la dignidad de la persona.\n\n" +
                        "Obra destacada: La ronda de noche."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Diego Velázquez",
                "Pintor barroco español, autor de Las meninas.",
                "Velázquez fue pintor de la corte española. Su obra destaca por la observación psicológica, la soltura técnica y la complejidad espacial.\n\n" +
                        "Las meninas es una de las pinturas más analizadas por su juego entre mirada, representación y realidad."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Francisco de Goya",
                "Artista español que conectó el arte clásico, moderno y crítico.",
                "Goya retrató la corte, la guerra, la superstición y la violencia humana. Su obra cambió con el tiempo hacia una visión más oscura y crítica.\n\n" +
                        "Obras importantes: El 3 de mayo de 1808, Saturno devorando a su hijo, Los caprichos."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Claude Monet",
                "Figura central del Impresionismo, interesado en la luz y el color.",
                "Monet pintó series de un mismo tema bajo distintas condiciones de luz, como nenúfares, catedrales y paisajes. Su obra muestra que la percepción cambia según el momento.\n\n" +
                        "Obra destacada: Impresión, sol naciente."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Vincent van Gogh",
                "Pintor postimpresionista famoso por su color intenso y pincelada emocional.",
                "Van Gogh desarrolló una pintura profundamente expresiva. Aunque tuvo poco reconocimiento en vida, hoy es uno de los artistas más influyentes.\n\n" +
                        "Obras importantes: La noche estrellada, Los girasoles, Terraza de café por la noche."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Pablo Picasso",
                "Uno de los artistas más influyentes del siglo XX y figura clave del Cubismo.",
                "Picasso exploró múltiples estilos durante su vida. Junto con Braque desarrolló el Cubismo, cambiando radicalmente la representación visual.\n\n" +
                        "Obras importantes: Guernica, Las señoritas de Avignon, El viejo guitarrista."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Salvador Dalí",
                "Artista surrealista conocido por imágenes oníricas y técnica detallada.",
                "Dalí combinó precisión académica con escenas imposibles, símbolos personales y humor extraño. Su figura pública también fue parte de su obra.\n\n" +
                        "Obra importante: La persistencia de la memoria."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Frida Kahlo",
                "Artista mexicana que exploró identidad, dolor, cuerpo, cultura y autorrepresentación.",
                "Frida Kahlo creó una obra profundamente personal. Sus autorretratos mezclan elementos autobiográficos, símbolos mexicanos, heridas físicas y emociones intensas.\n\n" +
                        "Obras importantes: Las dos Fridas, La columna rota, Autorretrato con collar de espinas."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Diego Rivera",
                "Muralista mexicano que integró arte, historia y crítica social.",
                "Diego Rivera fue una figura central del muralismo mexicano. Sus murales representan trabajadores, historia indígena, revolución, industria y lucha social.\n\n" +
                        "Obra destacada: Sueño de una tarde dominical en la Alameda Central."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Andy Warhol",
                "Figura central del Pop Art, interesado en fama, consumo e imágenes repetidas.",
                "Warhol usó imágenes de celebridades, productos comerciales y medios masivos. Su obra cuestiona la originalidad, la cultura de consumo y la relación entre arte y mercado.\n\n" +
                        "Obra destacada: Campbell’s Soup Cans."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Jean-Michel Basquiat",
                "Artista contemporáneo con lenguaje urbano, simbólico y crítico.",
                "Basquiat combinó texto, símbolos, figuras expresivas y referencias a historia afrodescendiente, poder, racismo y cultura urbana.\n\n" +
                        "Su obra mantiene una energía intensa y una crítica social directa."
        ));

        lista.add(new HistoriaArteItem(
                "Artistas",
                "Yayoi Kusama",
                "Artista japonesa reconocida por puntos, instalaciones inmersivas y repetición.",
                "Kusama trabaja con patrones, espejos, luces, calabazas y espacios envolventes. Su obra conecta obsesión, infinito, cuerpo y experiencia sensorial."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "Mona Lisa",
                "Obra de Leonardo da Vinci, famosa por su sonrisa enigmática y técnica sutil.",
                "La Mona Lisa es una de las pinturas más reconocidas del mundo. Su importancia se relaciona con el uso del sfumato, la mirada de la figura, el paisaje de fondo y el misterio de su expresión.\n\n" +
                        "Autor: Leonardo da Vinci.\n" +
                        "Periodo: Renacimiento.\n" +
                        "Importancia: representa el ideal renacentista de observación, técnica y ambigüedad psicológica."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "La última cena",
                "Mural de Leonardo da Vinci que representa el momento en que Jesús anuncia la traición.",
                "La última cena destaca por su composición, perspectiva y dramatismo psicológico. Los apóstoles reaccionan de formas distintas, creando una escena de tensión narrativa.\n\n" +
                        "Autor: Leonardo da Vinci.\n" +
                        "Periodo: Renacimiento."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "La creación de Adán",
                "Fresco de Miguel Ángel en la Capilla Sixtina.",
                "La creación de Adán muestra el momento simbólico en que Dios da vida al primer hombre. La cercanía de las manos se volvió una de las imágenes más icónicas del arte occidental.\n\n" +
                        "Autor: Miguel Ángel.\n" +
                        "Periodo: Renacimiento."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "Las meninas",
                "Pintura de Diego Velázquez que juega con la mirada, el espacio y la representación.",
                "Las meninas es una obra compleja porque involucra al pintor, la infanta, los reyes reflejados y al espectador. Pregunta quién mira a quién y qué significa representar una escena.\n\n" +
                        "Autor: Diego Velázquez.\n" +
                        "Periodo: Barroco."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "La noche estrellada",
                "Pintura de Van Gogh con cielo turbulento, color intenso y gran carga emocional.",
                "La noche estrellada representa un paisaje nocturno con remolinos, estrellas vibrantes y un ciprés oscuro. Es una obra clave del Postimpresionismo por su expresividad y ritmo visual.\n\n" +
                        "Autor: Vincent van Gogh."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "El grito",
                "Obra de Edvard Munch que expresa ansiedad, angustia y crisis interior.",
                "El grito es una imagen emblemática de la modernidad. La figura central parece emitir o escuchar un grito que atraviesa el paisaje. Sus líneas onduladas y colores intensos refuerzan la tensión emocional.\n\n" +
                        "Autor: Edvard Munch.\n" +
                        "Movimiento relacionado: Expresionismo."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "Guernica",
                "Obra de Picasso contra la violencia de la guerra.",
                "Guernica fue pintada como respuesta al bombardeo de la ciudad vasca del mismo nombre. En blanco, negro y gris, muestra figuras fragmentadas, dolor, caos y denuncia política.\n\n" +
                        "Autor: Pablo Picasso.\n" +
                        "Movimiento relacionado: Cubismo y arte político."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "La persistencia de la memoria",
                "Obra surrealista de Dalí famosa por sus relojes blandos.",
                "La persistencia de la memoria muestra un paisaje extraño con relojes derretidos. La obra suele interpretarse como una reflexión sobre el tiempo, el sueño y la percepción.\n\n" +
                        "Autor: Salvador Dalí.\n" +
                        "Movimiento: Surrealismo."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "Las dos Fridas",
                "Obra de Frida Kahlo sobre identidad, dolor y dualidad.",
                "Las dos Fridas muestra dos versiones de la artista tomadas de la mano, con corazones visibles y una conexión simbólica entre ambas. Refleja emociones personales y tensiones culturales.\n\n" +
                        "Autora: Frida Kahlo."
        ));

        lista.add(new HistoriaArteItem(
                "Obras importantes",
                "Sueño de una tarde dominical en la Alameda Central",
                "Mural de Diego Rivera que recorre personajes de la historia mexicana.",
                "Este mural reúne figuras históricas, políticas y culturales de México en un paseo simbólico por la Alameda Central. Es una obra clave del muralismo mexicano.\n\n" +
                        "Autor: Diego Rivera."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Dibujo",
                "Base fundamental del arte visual, usada para bocetar, estudiar y crear obras finales.",
                "El dibujo utiliza línea, tono, sombra y composición. Puede hacerse con grafito, carbón, tinta, pastel o medios digitales. Es importante porque ayuda a observar, planear y expresar ideas visuales."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Pintura al óleo",
                "Técnica que usa pigmentos mezclados con aceite, conocida por su profundidad y flexibilidad.",
                "El óleo permite transiciones suaves, colores intensos, veladuras y correcciones. Fue fundamental en el Renacimiento, Barroco y muchas etapas posteriores."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Acuarela",
                "Técnica de pintura con pigmentos diluidos en agua, transparente y luminosa.",
                "La acuarela requiere control del agua, capas ligeras y rapidez. Se usa mucho en paisajes, ilustración, estudios botánicos y pintura expresiva."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Acrílico",
                "Pintura de secado rápido, versátil y resistente.",
                "El acrílico puede usarse aguado como acuarela o espeso como óleo. Es popular en arte contemporáneo, muralismo, ilustración y técnicas mixtas."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Fresco",
                "Técnica mural realizada sobre yeso húmedo.",
                "El fresco se usó ampliamente en murales antiguos y renacentistas. El pigmento se integra al muro mientras el yeso seca, haciendo la obra muy duradera."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Escultura",
                "Arte de crear formas tridimensionales en materiales como piedra, madera, metal, barro o resina.",
                "La escultura trabaja volumen, espacio, textura y materialidad. Puede ser figurativa, abstracta, monumental, decorativa o conceptual."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Grabado",
                "Proceso que permite reproducir imágenes desde una matriz.",
                "El grabado puede hacerse en madera, metal, linóleo u otros soportes. Técnicas como xilografía, aguafuerte y litografía han sido importantes para difundir imágenes."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Collage",
                "Técnica que une fragmentos de imágenes, papel, tela u objetos sobre una superficie.",
                "El collage fue usado por cubistas, dadaístas y surrealistas. Permite crear nuevas relaciones visuales a partir de materiales existentes."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Fotografía",
                "Medio artístico basado en la captura de imágenes mediante luz.",
                "La fotografía transformó la historia del arte porque cambió la relación entre imagen, realidad y memoria. Puede ser documental, conceptual, artística, publicitaria o experimental."
        ));

        lista.add(new HistoriaArteItem(
                "Técnicas",
                "Arte digital",
                "Creación artística usando herramientas tecnológicas.",
                "Incluye ilustración digital, modelado 3D, animación, videoarte, realidad aumentada, inteligencia artificial, instalaciones interactivas y obras generadas por software."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Color",
                "Elemento visual que transmite emoción, atmósfera y significado.",
                "El color puede organizar una obra, crear contraste, representar luz o transmitir estados de ánimo. Conceptos importantes: tono, saturación, valor, armonía, temperatura y paleta."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Línea",
                "Elemento básico que define contornos, dirección, movimiento y estructura.",
                "La línea puede ser recta, curva, quebrada, continua, expresiva, delicada o agresiva. Es esencial en dibujo, diseño, pintura, grabado y composición."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Composición",
                "Organización de los elementos visuales dentro de una obra.",
                "La composición decide dónde se colocan figuras, colores, líneas, luces y espacios. Una buena composición guía la mirada y da equilibrio o tensión a la imagen."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Perspectiva",
                "Método para representar profundidad en una superficie plana.",
                "La perspectiva lineal usa puntos de fuga y líneas convergentes. Fue fundamental en el Renacimiento y cambió la manera de construir espacios visuales."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Claroscuro",
                "Uso contrastado de luz y sombra para crear volumen y dramatismo.",
                "El claroscuro ayuda a modelar figuras, generar profundidad y dirigir la atención. Fue muy importante en el Barroco, especialmente en Caravaggio y Rembrandt."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Textura",
                "Cualidad visual o táctil de una superficie.",
                "La textura puede ser real, como la rugosidad de una escultura, o visual, como pinceladas que sugieren piel, tela, piedra o movimiento."
        ));

        lista.add(new HistoriaArteItem(
                "Conceptos",
                "Simbolismo",
                "Uso de elementos visuales para representar ideas más allá de lo literal.",
                "Una flor, un animal, un color o un objeto pueden tener significados religiosos, políticos, personales o culturales según el contexto."
        ));

        lista.add(new HistoriaArteItem(
                "Arte mexicano",
                "Arte prehispánico",
                "Incluye expresiones de culturas como mexica, maya, zapoteca, olmeca y teotihuacana.",
                "El arte prehispánico integraba religión, astronomía, poder político y vida cotidiana. Destacan esculturas, cerámica, códices, arquitectura, relieves, máscaras y objetos rituales.\n\n" +
                        "Ejemplos importantes: la Piedra del Sol, esculturas olmecas, murales de Teotihuacan y arquitectura maya."
        ));

        lista.add(new HistoriaArteItem(
                "Arte mexicano",
                "Muralismo mexicano",
                "Movimiento artístico y social que llevó el arte a muros públicos.",
                "Después de la Revolución Mexicana, el muralismo buscó educar, representar al pueblo y narrar la historia nacional. Sus murales hablaron de indígenas, trabajadores, revolución, lucha social e identidad.\n\n" +
                        "Artistas principales: Diego Rivera, José Clemente Orozco y David Alfaro Siqueiros."
        ));

        lista.add(new HistoriaArteItem(
                "Arte mexicano",
                "Arte popular mexicano",
                "Incluye artesanías, textiles, cerámica, alebrijes, talavera, papel picado y objetos rituales.",
                "El arte popular mexicano combina tradición, comunidad, color, técnica y simbolismo. Muchas piezas se relacionan con fiestas, vida cotidiana, religión, identidad regional y transmisión familiar de saberes."
        ));

        lista.add(new HistoriaArteItem(
                "Arte mexicano",
                "Remedios Varo y Leonora Carrington",
                "Dos artistas vinculadas al surrealismo en México.",
                "Remedios Varo y Leonora Carrington desarrollaron mundos imaginarios llenos de alquimia, misterio, figuras femeninas, criaturas fantásticas y viajes interiores.\n\n" +
                        "Su obra enriqueció el surrealismo desde una mirada poética, simbólica y personal."
        ));

        lista.add(new HistoriaArteItem(
                "Arte actual",
                "Arte digital e inteligencia artificial",
                "La tecnología amplió las formas de crear, distribuir y experimentar arte.",
                "El arte digital incluye ilustración, animación, modelado 3D, video, instalaciones interactivas y obras generadas con algoritmos. La inteligencia artificial abrió nuevas preguntas sobre autoría, creatividad y colaboración entre humanos y máquinas."
        ));

        lista.add(new HistoriaArteItem(
                "Arte actual",
                "Redes sociales y portafolios digitales",
                "Las plataformas digitales cambiaron la forma en que artistas muestran y venden su obra.",
                "Hoy muchos artistas construyen comunidad en redes sociales, publican procesos, venden piezas, reciben encargos y conectan con públicos globales. El portafolio digital se volvió una herramienta central para la profesionalización artística."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Boceto",
                "Dibujo preliminar para planear una obra.",
                "Un boceto permite explorar ideas, composición, proporciones y detalles antes de crear la pieza final. Puede ser rápido y sencillo."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Paleta de color",
                "Conjunto de colores seleccionados para una obra.",
                "La paleta puede ser cálida, fría, contrastante, monocromática o armónica. Ayuda a definir atmósfera e identidad visual."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Figurativo",
                "Arte que representa figuras reconocibles.",
                "Puede mostrar personas, objetos, animales o paisajes. No necesariamente debe ser realista."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Abstracto",
                "Arte que no representa objetos reconocibles de forma directa.",
                "Se basa en color, forma, línea, ritmo y composición para crear una experiencia visual o emocional."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Instalación",
                "Obra que ocupa y transforma un espacio.",
                "Una instalación puede combinar objetos, sonido, luz, video, texto y recorrido del espectador."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Performance",
                "Acción artística realizada por el cuerpo o presencia del artista.",
                "Puede ocurrir en vivo o registrarse en video. Suele explorar identidad, tiempo, cuerpo, política o relación con el público."
        ));

        lista.add(new HistoriaArteItem(
                "Glosario",
                "Curaduría",
                "Proceso de selección, organización e interpretación de obras.",
                "La curaduría construye una narrativa para una exposición, colección o proyecto artístico."
        ));

        return lista;
    }
}