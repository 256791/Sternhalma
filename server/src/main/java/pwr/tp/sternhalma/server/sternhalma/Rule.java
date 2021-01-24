package pwr.tp.sternhalma.server.sternhalma;

/**
 * Interface defining way of checking rules
 */
public interface Rule {
    int isValid(Pone from, Field to, int player, int move);
}
