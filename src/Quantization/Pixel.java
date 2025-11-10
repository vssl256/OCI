package Quantization;

public class Pixel {
    private final int r, g, b;
    public double L, A, B;

    public Pixel(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.setLAB();
    }
    public static Pixel fromLAB( double L, double A, double B ) {
        double y = ( L + 16.0 ) / 116.0;
        double x = A / 500.0 + y;
        double z = y - B / 200.0;

        double x3 = Math.pow( x, 3 );
        double y3 = Math.pow( y, 3 );
        double z3 = Math.pow( z, 3 );

        x = ( x3 > 0.00856 ) ? x3 : ( x - 16.0 / 116.0 ) / 7.787;
        y = ( y3 > 0.00856 ) ? y3 : ( y - 16.0 / 116.0 ) / 7.787;
        z = ( z3 > 0.00856 ) ? z3 : ( z - 16.0 / 116.0 ) / 7.787;

        x *= 95.047;
        y *= 100.0;
        z *= 108.883;

        double R =  x *  0.032406 + y * (-0.015372) + z * (-0.004986);
        double G =  x * (-0.009689) + y *  0.018758 + z *  0.000415;
        double Bc = x *  0.000557 + y * (-0.002040) + z *  0.010570;

        R = (R > 0.0031308) ? (1.055 * Math.pow(R, 1/2.4) - 0.055) : 12.92 * R;
        G = (G > 0.0031308) ? (1.055 * Math.pow(G, 1/2.4) - 0.055) : 12.92 * G;
        Bc = (Bc > 0.0031308) ? (1.055 * Math.pow(Bc, 1/2.4) - 0.055) : 12.92 * Bc;

        int r = (int)Math.round(Math.max(0, Math.min(1, R)) * 255);
        int g = (int)Math.round(Math.max(0, Math.min(1, G)) * 255);
        int b = (int)Math.round(Math.max(0, Math.min(1, Bc)) * 255);

        return new Pixel( r, g, b );
    }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    public static double distance( Pixel a, Pixel b ) {
        double dL = a.L - b.L;
        double dA = a.A - b.A;
        double dB = a.B - b.B;
        return Math.sqrt( dL * dL + dA * dA + dB * dB );
    }

    public void setLAB() {
        int r = this.r, g = this.g, b = this.b;

        double CR = r / 255.0;
        double CG = g / 255.0;
        double CB = b / 255.0;

        double dark = 0.04045;
        double CRL = ( CR <= dark ) ? CR / 12.92 : Math.pow( ( CR + 0.055 ) / 1.055, 2.4 );
        double CGL = ( CG <= dark ) ? CG / 12.92 : Math.pow( ( CG + 0.055 ) / 1.055, 2.4 );
        double CBL = ( CB <= dark ) ? CB / 12.92 : Math.pow( ( CB + 0.055 ) / 1.055, 2.4 );

        double X = ( CRL * 0.4124 + CGL * 0.3576 + CBL * 0.1805 ) * 100.0;
        double Y = ( CRL * 0.2126 + CGL * 0.7152 + CBL * 0.0722 ) * 100.0;
        double Z = ( CRL * 0.0193 + CGL * 0.1192 + CBL * 0.9505 ) * 100.0;

        double x = X / 95.047;
        double y = Y / 100.0;
        double z = Z / 108.883;

        this.L = 116 * f( y ) - 16;
        this.A = 500 * ( f( x ) - f( y ) );
        this.B = 200 * ( f( y ) - f( z ) );
    }

    public double f( double t ) {
        return ( t <= 0.008856 ) ? 7.787 * t + 16.0 / 116.0 : Math.pow( t, 1.0 / 3.0 );
    }
}
