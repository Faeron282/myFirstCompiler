package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class CodeGenerator {
    // There are some Tests this compiler fails that work when i run it locally (for example: Multiplication Precedence for Addition) switching _start to main doesnt change anything so I will just leave it as is for now
    // Not saying this compiler passes all tests or is any good for that matter but it should be better than it is now
    public String generateCode(List<IrGraph> program) {
        StringBuilder builder = new StringBuilder();

        builder.append(".global _start\n")
               .append(".global _main\n")
               .append(".text\n")
               .append("_start:\n")
               .append("  call _main\n")
               .append("  mov %eax, %edi\n")
               .append("  mov $60, %rax\n")
               .append("  syscall\n\n")
               .append("_main:\n");

        for (IrGraph graph : program) {
            AasmRegisterAllocator allocator = new AasmRegisterAllocator();
            Map<Node, Register> registers = allocator.allocateRegisters(graph);


            generateForGraph(graph, builder, registers);
        }

        builder.append("  ret\n");
        return builder.toString();
    }

    private void generateForGraph(IrGraph graph, StringBuilder builder, Map<Node, Register> registers) {
        Set<Node> visited = new HashSet<>();
        scan(graph.endBlock(), visited, builder, registers);
    }

    private void scan(Node node, Set<Node> visited, StringBuilder builder, Map<Node, Register> registers) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, builder, registers);
            }
        }

        switch (node) {
            case AddNode add -> binary(builder, registers, add, "addl");
            case SubNode sub -> binary(builder, registers, sub, "subl");
            case MulNode mul -> binary(builder, registers, mul, "imull");
            case DivNode div -> {
                Node left = predecessorSkipProj(div, BinaryOperationNode.LEFT);
                Node right = predecessorSkipProj(div, BinaryOperationNode.RIGHT);
                builder.append("  movl $0, %edx\n");
                emitLoad(builder, registers, left, "%eax");
                emitLoad(builder, registers, right, "%ecx");
                builder.append("  idivl %ecx\n");
            }
            case ModNode mod -> {
                Node left = predecessorSkipProj(mod, BinaryOperationNode.LEFT);
                Node right = predecessorSkipProj(mod, BinaryOperationNode.RIGHT);
                builder.append("  movl $0, %edx\n");
                emitLoad(builder, registers, left, "%eax");
                emitLoad(builder, registers, right, "%ecx");
                builder.append("  idivl %ecx\n");
                builder.append("  movl %edx, %eax\n");
            }
            case ReturnNode r -> {
                Node retVal = predecessorSkipProj(r, ReturnNode.RESULT);
                emitLoad(builder, registers, retVal, "%eax");
            }
            case ConstIntNode c -> {
    Register reg = registers.get(c);
    builder.append("  movl $")
           .append(c.value())
           .append(", ")
           .append(reg)
           .append("\n");
}
            case Phi _ -> throw new UnsupportedOperationException("phi not supported yet");
            case Block _, ProjNode _, StartNode _ -> {
                return;
            }
            default -> throw new UnsupportedOperationException("Unhandled node type: " + node.getClass());
        }
    }

  private static void binary(StringBuilder builder, Map<Node, Register> registers, BinaryOperationNode node, String instr) {
    Node left = predecessorSkipProj(node, BinaryOperationNode.LEFT);
    Node right = predecessorSkipProj(node, BinaryOperationNode.RIGHT);
    Register target = registers.get(node);
    Register leftReg = registers.get(left);
    Register rightReg = registers.get(right);

    if (!target.equals(leftReg)) {
        builder.append("  movl ")
               .append(leftReg)
               .append(", ")
               .append(target)
               .append("\n");
    }

    builder.append("  ")
           .append(instr)
           .append(" ")
           .append(rightReg)
           .append(", ")
           .append(target)
           .append("\n");
}


    private static void emitLoad(StringBuilder builder, Map<Node, Register> registers, Node node, String targetReg) {
        if (node instanceof ConstIntNode c) {
            builder.append("  movl $")
                   .append(c.value())
                   .append(", ")
                   .append(targetReg)
                   .append("\n");
        } else {
            builder.append("  movl ")
                   .append(registers.get(node))
                   .append(", ")
                   .append(targetReg)
                   .append("\n");
        }
    }
}
