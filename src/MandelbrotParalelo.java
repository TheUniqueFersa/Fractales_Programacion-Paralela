import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import javax.swing.*;

public class MandelbrotParalelo extends JPanel {
    private static final int ANCHO = 1000;
    private static final int ALTO = 800;
    private static int ITERACIONES_MAXIMAS = 10000;
    private static final double ZOOM_BASE = 300;

    private BufferedImage imagen;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;
    private double factorZoom = 1;

    public MandelbrotParalelo(int numHilos) {
        imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        generarFractal(numHilos);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evento) {
                int x = evento.getX();
                int y = evento.getY();

                // nuevo centro
                desplazamientoX += (x - ANCHO / 2.0) / (ZOOM_BASE * factorZoom);
                desplazamientoY += (y - ALTO / 2.0) / (ZOOM_BASE * factorZoom);

                // Ajusta el zoom
                if (evento.getButton() == java.awt.event.MouseEvent.BUTTON1) { // Zoom in
                    factorZoom *= 1.5;
                } else if (evento.getButton() == java.awt.event.MouseEvent.BUTTON3) { // Zoom out
                    factorZoom /= 1.5;
                }

                // Mide el tiempo de generación
                long tiempoInicio = System.nanoTime();
                generarFractal(numHilos);
                long tiempoFin = System.nanoTime();
                System.out.println("Tiempo de generación: " + (tiempoFin - tiempoInicio) / 1_000_000 + " ms");

                repaint();
            }
        });
    }

    private void generarFractal(int numHilos) {
        ForkJoinPool pool = new ForkJoinPool(numHilos);
        pool.invoke(new TareaMandelbrot(0, ANCHO, imagen));
    }

    private class TareaMandelbrot extends RecursiveAction {
        private static final int UMBRAL = 100;
        private int inicioX, finX;
        private BufferedImage imagen;

        TareaMandelbrot(int inicioX, int finX, BufferedImage imagen) {
            this.inicioX = inicioX;
            this.finX = finX;
            this.imagen = imagen;
        }

        @Override
        protected void compute() {
            if (finX - inicioX <= UMBRAL) {
                calcularDirectamente();
            } else {
                int medioX = (inicioX + finX) / 2;
                TareaMandelbrot izquierda = new TareaMandelbrot(inicioX, medioX, imagen);
                TareaMandelbrot derecha = new TareaMandelbrot(medioX, finX, imagen);
                invokeAll(izquierda, derecha);
            }
        }

        private void calcularDirectamente() {
            for (int x = inicioX; x < finX; x++) {
                for (int y = 0; y < ALTO; y++) {
                    double zx = 0, zy = 0;
                    double cX = (x - ANCHO / 2.0) / (ZOOM_BASE * factorZoom) + desplazamientoX;
                    double cY = (y - ALTO / 2.0) / (ZOOM_BASE * factorZoom) + desplazamientoY;
                    int iter = 0;
                    while (zx * zx + zy * zy < 4 && iter < ITERACIONES_MAXIMAS) {
                        double tmp = zx * zx - zy * zy + cX;
                        zy = 2.0 * zx * zy + cY;
                        zx = tmp;
                        iter++;
                    }
                    int color = iter < ITERACIONES_MAXIMAS ? Color.HSBtoRGB(iter / 256f, 1, iter / (iter + 8f)) : 0;
                    imagen.setRGB(x, y, color);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagen, 0, 0, null);
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Fractal Mandelbrot - Paralelo");
        if (args.length > 0)
            MandelbrotParalelo.ITERACIONES_MAXIMAS = Integer.parseInt(args[0]);
        else
        MandelbrotParalelo.ITERACIONES_MAXIMAS = 10000;
        int numeroHilos = Runtime.getRuntime().availableProcessors(); // Pa obtener el número de hilos disponibles
        System.out.println("Número de hilos: " + numeroHilos);

        MandelbrotParalelo panel = new MandelbrotParalelo(numeroHilos);
        

        ventana.add(panel);
        ventana.setSize(ANCHO, ALTO);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);
    }
}
