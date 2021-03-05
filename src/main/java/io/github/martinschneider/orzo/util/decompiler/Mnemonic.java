package io.github.martinschneider.orzo.util.decompiler;

public class Mnemonic {
  public static final String[] OPCODE = {
    "nop", /* 0 */
    "aconst_null", /* 1 */
    "iconst_m1", /* 2 */
    "iconst_0", /* 3 */
    "iconst_1", /* 4 */
    "iconst_2", /* 5 */
    "iconst_3", /* 6 */
    "iconst_4", /* 7 */
    "iconst_5", /* 8 */
    "lconst_0", /* 9 */
    "lconst_1", /* 10 */
    "fconst_0", /* 11 */
    "fconst_1", /* 12 */
    "fconst_2", /* 13 */
    "dconst_0", /* 14 */
    "dconst_1", /* 15 */
    "bipush", /* 16 */
    "sipush", /* 17 */
    "ldc", /* 18 */
    "ldc_w", /* 19 */
    "ldc2_w", /* 20 */
    "iload", /* 21 */
    "lload", /* 22 */
    "fload", /* 23 */
    "dload", /* 24 */
    "aload", /* 25 */
    "iload_0", /* 26 */
    "iload_1", /* 27 */
    "iload_2", /* 28 */
    "iload_3", /* 29 */
    "lload_0", /* 30 */
    "lload_1", /* 31 */
    "lload_2", /* 32 */
    "lload_3", /* 33 */
    "fload_0", /* 34 */
    "fload_1", /* 35 */
    "fload_2", /* 36 */
    "fload_3", /* 37 */
    "dload_0", /* 38 */
    "dload_1", /* 39 */
    "dload_2", /* 40 */
    "dload_3", /* 41 */
    "aload_0", /* 42 */
    "aload_1", /* 43 */
    "aload_2", /* 44 */
    "aload_3", /* 45 */
    "iaload", /* 46 */
    "laload", /* 47 */
    "faload", /* 48 */
    "daload", /* 49 */
    "aaload", /* 50 */
    "baload", /* 51 */
    "caload", /* 52 */
    "saload", /* 53 */
    "istore", /* 54 */
    "lstore", /* 55 */
    "fstore", /* 56 */
    "dstore", /* 57 */
    "astore", /* 58 */
    "istore_0", /* 59 */
    "istore_1", /* 60 */
    "istore_2", /* 61 */
    "istore_3", /* 62 */
    "lstore_0", /* 63 */
    "lstore_1", /* 64 */
    "lstore_2", /* 65 */
    "lstore_3", /* 66 */
    "fstore_0", /* 67 */
    "fstore_1", /* 68 */
    "fstore_2", /* 69 */
    "fstore_3", /* 70 */
    "dstore_0", /* 71 */
    "dstore_1", /* 72 */
    "dstore_2", /* 73 */
    "dstore_3", /* 74 */
    "astore_0", /* 75 */
    "astore_1", /* 76 */
    "astore_2", /* 77 */
    "astore_3", /* 78 */
    "iastore", /* 79 */
    "lastore", /* 80 */
    "fastore", /* 81 */
    "dastore", /* 82 */
    "aastore", /* 83 */
    "bastore", /* 84 */
    "castore", /* 85 */
    "sastore", /* 86 */
    "pop", /* 87 */
    "pop2", /* 88 */
    "dup", /* 89 */
    "dup_x1", /* 90 */
    "dup_x2", /* 91 */
    "dup2", /* 92 */
    "dup2_x1", /* 93 */
    "dup2_x2", /* 94 */
    "swap", /* 95 */
    "iadd", /* 96 */
    "ladd", /* 97 */
    "fadd", /* 98 */
    "dadd", /* 99 */
    "isub", /* 100 */
    "lsub", /* 101 */
    "fsub", /* 102 */
    "dsub", /* 103 */
    "imul", /* 104 */
    "lmul", /* 105 */
    "fmul", /* 106 */
    "dmul", /* 107 */
    "idiv", /* 108 */
    "ldiv", /* 109 */
    "fdiv", /* 110 */
    "ddiv", /* 111 */
    "irem", /* 112 */
    "lrem", /* 113 */
    "frem", /* 114 */
    "drem", /* 115 */
    "ineg", /* 116 */
    "lneg", /* 117 */
    "fneg", /* 118 */
    "dneg", /* 119 */
    "ishl", /* 120 */
    "lshl", /* 121 */
    "ishr", /* 122 */
    "lshr", /* 123 */
    "iushr", /* 124 */
    "lushr", /* 125 */
    "iand", /* 126 */
    "land", /* 127 */
    "ior", /* 128 */
    "lor", /* 129 */
    "ixor", /* 130 */
    "lxor", /* 131 */
    "iinc", /* 132 */
    "i2l", /* 133 */
    "i2f", /* 134 */
    "i2d", /* 135 */
    "l2i", /* 136 */
    "l2f", /* 137 */
    "l2d", /* 138 */
    "f2i", /* 139 */
    "f2l", /* 140 */
    "f2d", /* 141 */
    "d2i", /* 142 */
    "d2l", /* 143 */
    "d2f", /* 144 */
    "i2b", /* 145 */
    "i2c", /* 146 */
    "i2s", /* 147 */
    "lcmp", /* 148 */
    "fcmpl", /* 149 */
    "fcmpg", /* 150 */
    "dcmpl", /* 151 */
    "dcmpg", /* 152 */
    "ifeq", /* 153 */
    "ifne", /* 154 */
    "iflt", /* 155 */
    "ifge", /* 156 */
    "ifgt", /* 157 */
    "ifle", /* 158 */
    "if_icmpeq", /* 159 */
    "if_icmpne", /* 160 */
    "if_icmplt", /* 161 */
    "if_icmpge", /* 162 */
    "if_icmpgt", /* 163 */
    "if_icmple", /* 164 */
    "if_acmpeq", /* 165 */
    "if_acmpne", /* 166 */
    "goto", /* 167 */
    "jsr", /* 168 */
    "ret", /* 169 */
    "tableswitch", /* 170 */
    "lookupswitch", /* 171 */
    "ireturn", /* 172 */
    "lreturn", /* 173 */
    "freturn", /* 174 */
    "dreturn", /* 175 */
    "areturn", /* 176 */
    "return", /* 177 */
    "getstatic", /* 178 */
    "putstatic", /* 179 */
    "getfield", /* 180 */
    "putfield", /* 181 */
    "invokevirtual", /* 182 */
    "invokespecial", /* 183 */
    "invokestatic", /* 184 */
    "invokeinterface", /* 185 */
    "invokedynamic", /* 186 */
    "new", /* 187 */
    "newarray", /* 188 */
    "anewarray", /* 189 */
    "arraylength", /* 190 */
    "athrow", /* 191 */
    "checkcast", /* 192 */
    "instanceof", /* 193 */
    "monitorenter", /* 194 */
    "monitorexit", /* 195 */
    "wide", /* 196 */
    "multianewarray", /* 197 */
    "ifnull", /* 198 */
    "ifnonnull", /* 199 */
    "goto_w", /* 200 */
    "jsr_w" /* 201 */
  };

  public enum ParameterType {
    EMPTY,
    BYTE,
    UNSIGNED_BYTE,
    TWO_BYTES,
    SHORT,
    UNSIGNED_SHORT,
    SHORT_PLUS_ONE_BYTE,
    SHORT_PLUS_TWO_BYTES,
    TWO_SHORTS,
    INT,
    SPECIAL
  }

  public static final ParameterType[] ADDITIONAL_BYTES = {
    ParameterType.EMPTY, /* nop 0 */
    ParameterType.EMPTY, /* aconst_null (1) */
    ParameterType.EMPTY,
    /* iconst_m1 (2) */ ParameterType.EMPTY, /* iconst_0 (3) */
    ParameterType.EMPTY, /* iconst_1 (4) */
    ParameterType.EMPTY, /* iconst_2 (5) */
    ParameterType.EMPTY, /* iconst_3 (6) */
    ParameterType.EMPTY, /* iconst_4 (7) */
    ParameterType.EMPTY, /* iconst_5 (8) */
    ParameterType.EMPTY, /* lconst_0 (9) */
    ParameterType.EMPTY, /* lconst_1 (10) */
    ParameterType.EMPTY, /* fconst_0 (11) */
    ParameterType.EMPTY, /* fconst_1 (12) */
    ParameterType.EMPTY, /* fconst_2 (13) */
    ParameterType.EMPTY, /* dconst_0 (14) */
    ParameterType.EMPTY, /* dconst_1 (15) */
    ParameterType.BYTE, /* bipush (16) */
    ParameterType.SHORT, /* sipush (17) */
    ParameterType.BYTE, /* ldc (18) */
    ParameterType.SHORT, /* ldc_w (19) */
    ParameterType.SHORT, /* ldc2_w (20) */
    ParameterType.BYTE, /* iload (21) */
    ParameterType.BYTE, /* lload (22) */
    ParameterType.BYTE, /* fload (23) */
    ParameterType.BYTE, /* dload (24) */
    ParameterType.BYTE, /* aload (25) */
    ParameterType.EMPTY, /* iload_0 (26) */
    ParameterType.EMPTY, /* iload_1 (27) */
    ParameterType.EMPTY, /* iload_2 (28) */
    ParameterType.EMPTY, /* iload_3 (29) */
    ParameterType.EMPTY, /* lload_0 (30) */
    ParameterType.EMPTY, /* lload_1 (31) */
    ParameterType.EMPTY, /* lload_2 (32) */
    ParameterType.EMPTY, /* lload_3 (33) */
    ParameterType.EMPTY, /* fload_0 (34) */
    ParameterType.EMPTY, /* fload_1 (35) */
    ParameterType.EMPTY, /* fload_2 (36) */
    ParameterType.EMPTY, /* fload_3 (37) */
    ParameterType.EMPTY, /* dload_0 (38) */
    ParameterType.EMPTY, /* dload_1 (39) */
    ParameterType.EMPTY, /* dload_2 (40) */
    ParameterType.EMPTY, /* dload_3 (41) */
    ParameterType.EMPTY, /* aload_0 (42) */
    ParameterType.EMPTY, /* aload_1 (43) */
    ParameterType.EMPTY, /* aload_2 (44) */
    ParameterType.EMPTY, /* aload_3 (45) */
    ParameterType.EMPTY, /* iaload (46) */
    ParameterType.EMPTY, /* laload (47) */
    ParameterType.EMPTY, /* faload (48) */
    ParameterType.EMPTY, /* daload (49) */
    ParameterType.EMPTY, /* aaload (50) */
    ParameterType.EMPTY, /* baload (51) */
    ParameterType.EMPTY, /* caload (52) */
    ParameterType.EMPTY, /* saload (53) */
    ParameterType.BYTE, /* istore (54) */
    ParameterType.BYTE, /* lstore (55) */
    ParameterType.BYTE, /* fstore (56) */
    ParameterType.BYTE, /* dstore (57) */
    ParameterType.BYTE, /* astore (58) */
    ParameterType.EMPTY, /* istore_0 (59) */
    ParameterType.EMPTY, /* istore_1 (60) */
    ParameterType.EMPTY, /* istore_2 (61) */
    ParameterType.EMPTY, /* istore_3 (62) */
    ParameterType.EMPTY, /* lstore_0 (63) */
    ParameterType.EMPTY, /* lstore_1 (64) */
    ParameterType.EMPTY, /* lstore_2 (65) */
    ParameterType.EMPTY, /* lstore_3 (66) */
    ParameterType.EMPTY, /* fstore_0 (67) */
    ParameterType.EMPTY, /* fstore_1 (68) */
    ParameterType.EMPTY, /* fstore_2 (69) */
    ParameterType.EMPTY, /* fstore_3 (70) */
    ParameterType.EMPTY, /* dstore_0 (71) */
    ParameterType.EMPTY, /* dstore_1 (72) */
    ParameterType.EMPTY, /* dstore_2 (73) */
    ParameterType.EMPTY, /* dstore_3 (74) */
    ParameterType.EMPTY, /* astore_0 (75) */
    ParameterType.EMPTY, /* astore_1 (76) */
    ParameterType.EMPTY, /* astore_2 (77) */
    ParameterType.EMPTY, /* astore_3 (78) */
    ParameterType.EMPTY, /* iastore (79) */
    ParameterType.EMPTY, /* lastore (80) */
    ParameterType.EMPTY, /* fastore (81) */
    ParameterType.EMPTY, /* dastore (82) */
    ParameterType.EMPTY, /* aastore (83) */
    ParameterType.EMPTY, /* bastore (84) */
    ParameterType.EMPTY, /* castore (85) */
    ParameterType.EMPTY, /* sastore (86) */
    ParameterType.EMPTY, /* pop (87) */
    ParameterType.EMPTY, /* pop2 (88) */
    ParameterType.EMPTY, /* dup (89) */
    ParameterType.EMPTY, /* dup_x1 (90) */
    ParameterType.EMPTY, /* dup_x2 (91) */
    ParameterType.EMPTY, /* dup2 (92) */
    ParameterType.EMPTY, /* dup2_x1 (93) */
    ParameterType.EMPTY, /* dup2_x2 (94) */
    ParameterType.EMPTY, /* swap (95) */
    ParameterType.EMPTY, /* iadd (96) */
    ParameterType.EMPTY, /* ladd (97) */
    ParameterType.EMPTY, /* fadd (98) */
    ParameterType.EMPTY, /* dadd (99) */
    ParameterType.EMPTY, /*
        * isub (100)
        */
    ParameterType.EMPTY, /* lsub (101) */
    ParameterType.EMPTY, /* fsub (102) */
    ParameterType.EMPTY, /* dsub (103) */
    ParameterType.EMPTY, /* imul (104) */
    ParameterType.EMPTY, /* lmul (105) */
    ParameterType.EMPTY, /* fmul (106) */
    ParameterType.EMPTY, /* dmul (107) */
    ParameterType.EMPTY, /* idiv (108) */
    ParameterType.EMPTY, /* ldiv (109) */
    ParameterType.EMPTY, /* fdiv (110) */
    ParameterType.EMPTY, /* ddiv (111) */
    ParameterType.EMPTY, /* irem (112) */
    ParameterType.EMPTY, /* lrem (113) */
    ParameterType.EMPTY, /* frem (114) */
    ParameterType.EMPTY, /* drem (115) */
    ParameterType.EMPTY, /* ineg (116) */
    ParameterType.EMPTY, /* lneg (117) */
    ParameterType.EMPTY, /* fneg (118) */
    ParameterType.EMPTY, /* dneg (119) */
    ParameterType.EMPTY, /* ishl (120) */
    ParameterType.EMPTY, /* lshl (121) */
    ParameterType.EMPTY, /* ishr (122) */
    ParameterType.EMPTY, /* lshr (123) */
    ParameterType.EMPTY, /* iushr (124) */
    ParameterType.EMPTY, /* lushr (125) */
    ParameterType.EMPTY, /* iand (126) */
    ParameterType.EMPTY, /* land (127) */
    ParameterType.EMPTY, /* ior (128) */
    ParameterType.EMPTY, /* lor (129) */
    ParameterType.EMPTY, /* ixor (130) */
    ParameterType.EMPTY, /* lxor (131) */
    ParameterType.TWO_BYTES, /* iinc (132) */
    ParameterType.EMPTY, /* i2l (133) */
    ParameterType.EMPTY, /* i2f (134) */
    ParameterType.EMPTY, /* i2d (135) */
    ParameterType.EMPTY, /* l2i (136) */
    ParameterType.EMPTY, /* l2f (137) */
    ParameterType.EMPTY, /* l2d (138) */
    ParameterType.EMPTY, /* f2i (139) */
    ParameterType.EMPTY, /* f2l (140) */
    ParameterType.EMPTY, /* f2d (141) */
    ParameterType.EMPTY, /* d2i (142) */
    ParameterType.EMPTY, /* d2l (143) */
    ParameterType.EMPTY, /* d2f (144) */
    ParameterType.EMPTY, /* i2b (145) */
    ParameterType.EMPTY, /* i2c (146) */
    ParameterType.EMPTY, /* i2s (147) */
    ParameterType.EMPTY, /* lcmp (148) */
    ParameterType.EMPTY, /* fcmpl (149) */
    ParameterType.EMPTY, /* fcmpg (150) */
    ParameterType.EMPTY, /* dcmpl (151) */
    ParameterType.EMPTY, /* dcmpg (152) */
    ParameterType.UNSIGNED_SHORT, /* ifeq (153) */
    ParameterType.UNSIGNED_SHORT, /* ifne (154) */
    ParameterType.UNSIGNED_SHORT, /* iflt (155) */
    ParameterType.UNSIGNED_SHORT, /* ifge (156) */
    ParameterType.UNSIGNED_SHORT, /* ifgt (157) */
    ParameterType.UNSIGNED_SHORT, /* ifle (158) */
    ParameterType.UNSIGNED_SHORT, /* if_icmpeq (159) */
    ParameterType.UNSIGNED_SHORT, /* if_icmpne (160) */
    ParameterType.UNSIGNED_SHORT, /* if_icmplt (161) */
    ParameterType.UNSIGNED_SHORT, /* if_icmpge (162) */
    ParameterType.UNSIGNED_SHORT, /* if_icmpgt (163) */
    ParameterType.UNSIGNED_SHORT, /* if_icmple (164) */
    ParameterType.UNSIGNED_SHORT, /* if_acmpeq (165) */
    ParameterType.UNSIGNED_SHORT, /* if_acmpne (166) */
    ParameterType.UNSIGNED_SHORT, /* goto (167) */
    ParameterType.SHORT, /* jsr (168) */
    ParameterType.BYTE, /* ret (169) */
    ParameterType.SPECIAL, /*
         * tableswitch
         * (170)
         * this
         * requires
         * special
         * handling
         * because
         * the
         * number
         * of
         * bytes
         * is
         * flexible
         */
    ParameterType.SPECIAL, /*
        * lookupswitch (171) this requires special handling because the number of bytes is
        * flexible
        */
    ParameterType.EMPTY, /* ireturn (172) */
    ParameterType.EMPTY, /* lreturn (173) */
    ParameterType.EMPTY, /* freturn (174) */
    ParameterType.EMPTY, /* dreturn (175) */
    ParameterType.EMPTY, /* areturn (176) */
    ParameterType.EMPTY, /* return (177) */
    ParameterType.UNSIGNED_SHORT, /* getstatic (178) */
    ParameterType.UNSIGNED_SHORT, /* putstatic (179) */
    ParameterType.UNSIGNED_SHORT, /* getfield (180) */
    ParameterType.UNSIGNED_SHORT, /* putfield (181) */
    ParameterType.UNSIGNED_SHORT, /* invokevirtual (182) */
    ParameterType.UNSIGNED_SHORT, /* invokespecial (183) */
    ParameterType.UNSIGNED_SHORT, /* invokestatic (184) */
    ParameterType.SHORT_PLUS_TWO_BYTES, /* invokeParameterType.INTerface (185) */
    ParameterType.SHORT_PLUS_TWO_BYTES, /* invokedynamic (186) */
    ParameterType.SHORT, /* new (187) */
    ParameterType.BYTE, /* newarray (188) */
    ParameterType.SHORT, /* anewarray (189) */
    ParameterType.EMPTY, /* arraylength (190) */
    ParameterType.EMPTY, /* athrow (191) */
    ParameterType.SHORT, /* checkcast (192) */
    ParameterType.SHORT, /* instanceof (193) */
    ParameterType.EMPTY, /* monitorenter (194) */
    ParameterType.EMPTY, /* monitorexit (195) */
    ParameterType.SPECIAL, /*
        * wide (196) this requires
        * special handling because the
        * number of bytes can be 3 or 5
        */
    ParameterType.SHORT_PLUS_ONE_BYTE, /* multianewarray (197) */
    ParameterType.SHORT, /* ifnull (198) */
    ParameterType.SHORT, /* ifnonnull (199) */
    ParameterType.INT, /* goto_w (200) */
    ParameterType.INT, /* jsr_w (201) */
  };
}
