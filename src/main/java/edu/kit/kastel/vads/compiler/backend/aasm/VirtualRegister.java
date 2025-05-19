package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public record VirtualRegister(int id) implements Register {
    @Override
    @Override
    public String toString() {
        return switch (id) {
            case 0  -> "%eax";
            case 1  -> "%ebx";
            case 2  -> "%ecx";
            case 3  -> "%edx";
            case 4  -> "%esi";
            case 5  -> "%edi";
            case 6  -> "%r8d";
            case 7  -> "%r9d";
            case 8  -> "%r10d";
            case 9  -> "%r11d";
            case 10 -> "%r12d";
            case 11 -> "%r13d";
            case 12 -> "%r14d";
            case 13 -> "%r15d";
            default -> "-" + (4 * (id - 13)) + "(%rbp)"; // Stackslot bei Offset
        };
    }

}


