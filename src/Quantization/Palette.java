package Quantization;

import java.util.ArrayList;
import java.util.List;

public class Palette {
    public static final int[] R = { 0x00, 0x33, 0x66, 0x99, 0xCC, 0xFF };
    public static final int[] G = { 0x00, 0x24, 0x49, 0x6D, 0x92, 0xB6, 0xDB, 0xFF };
    public static final int[] B = { 0x00, 0x40, 0x80, 0xC0, 0xFF };

    public static ArrayList<Pixel> def = new ArrayList<>();

    public static void initDef() {
        for ( int r : Palette.R ) {
            for ( int g : Palette.G ) {
                for ( int b : Palette.B ) {
                    Pixel pixel = new Pixel( r, g, b );
                    def.add( pixel );
                }
            }
        }
    }

    public static void print( List<Pixel> palette) {
        for (Pixel p : palette) {
            //String hex = String.format("#%02X%02X%02X", p.getR(), p.getG(), p.getB());

            System.out.print("\u001B[48;2;" + p.getR() + ";" + p.getG() + ";" + p.getB() + "m  \u001B[0m" + " ");
            //System.out.println(hex);
        }
        System.out.println();
    }
}
