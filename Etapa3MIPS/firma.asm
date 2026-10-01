.data
archivo_entrada: .asciiz "mips_input.txt"
archivo_salida:  .asciiz "firma.txt"

buffer_entrada: .space 128
.align 2
valores:        .space 12
resultado_mem:  .word 0
operaciones_mem:.word 0
estado_mem:     .word 0
checksum_mem:   .word 0
constante_17:   .word 17

texto_result:   .asciiz "RESULT="
texto_ops:      .asciiz "OPERATIONS="
texto_checksum: .asciiz "CHECKSUM="
texto_status:   .asciiz "STATUS="
texto_ok:       .asciiz "OK"
texto_error:    .asciiz "ERROR"
salto_linea:    .asciiz "\n"
numero_buffer:  .space 16

.text
.globl main

main:
    # Abrir mips_input.txt para lectura.
    li $v0, 13
    la $a0, archivo_entrada
    li $a1, 0
    li $a2, 0
    syscall
    move $s0, $v0
    bltz $s0, error_entrada

    # Leer el archivo completo en memoria.
    li $v0, 14
    move $a0, $s0
    la $a1, buffer_entrada
    li $a2, 128
    syscall

    li $v0, 16
    move $a0, $s0
    syscall

    # Recorrer el buffer y convertir las tres lineas a enteros.
    la $t0, buffer_entrada
    la $t1, valores
    li $t2, 0
    li $t3, 3
    li $t4, 0
    li $t7, 1

ciclo_parseo:
    beq $t2, $t3, fin_parseo
    lb $t5, 0($t0)
    beq $t5, $zero, guardar_numero_final

    li $t6, 10
    beq $t5, $t6, guardar_numero

    li $t6, 13
    beq $t5, $t6, avanzar_caracter

    li $t6, 45
    beq $t5, $t6, marcar_negativo

    addi $t5, $t5, -48
    mul $t4, $t4, 10
    add $t4, $t4, $t5

avanzar_caracter:
    addi $t0, $t0, 1
    j ciclo_parseo

marcar_negativo:
    li $t7, -1
    addi $t0, $t0, 1
    j ciclo_parseo

guardar_numero:
    mul $t4, $t4, $t7
    sw $t4, 0($t1)
    addi $t1, $t1, 4
    addi $t2, $t2, 1
    li $t4, 0
    li $t7, 1
    addi $t0, $t0, 1
    j ciclo_parseo

guardar_numero_final:
    mul $t4, $t4, $t7
    sw $t4, 0($t1)
    addi $t2, $t2, 1
    j ciclo_parseo

fin_parseo:
    # Copiar los valores leidos a variables con nombre.
    la $t0, valores
    lw $s1, 0($t0)
    lw $s2, 4($t0)
    lw $s3, 8($t0)

    sw $s1, resultado_mem
    sw $s2, operaciones_mem
    sw $s3, estado_mem

    # Si estado es 0, se registra error y checksum 0.
    beq $s3, $zero, estado_error

    # checksum = resultado XOR operaciones + 17.
    move $s4, $s1
    xor $s4, $s4, $s2
    lw $t0, constante_17
    add $s4, $s4, $t0
    sw $s4, checksum_mem
    j escribir_firma

estado_error:
    li $s4, 0
    sw $s4, checksum_mem
    j escribir_firma

error_entrada:
    li $s1, 0
    li $s2, 0
    li $s3, 0
    li $s4, 0
    sw $s4, checksum_mem

escribir_firma:
    # Crear firma.txt.
    li $v0, 13
    la $a0, archivo_salida
    li $a1, 1
    li $a2, 0
    syscall
    move $s5, $v0

    # RESULT=<numero>
    move $a0, $s5
    la $a1, texto_result
    li $a2, 7
    jal escribir_texto
    move $a0, $s1
    jal entero_a_texto
    move $a0, $s5
    move $a1, $v0
    move $a2, $v1
    jal escribir_texto
    jal escribir_salto

    # OPERATIONS=<numero>
    move $a0, $s5
    la $a1, texto_ops
    li $a2, 11
    jal escribir_texto
    move $a0, $s2
    jal entero_a_texto
    move $a0, $s5
    move $a1, $v0
    move $a2, $v1
    jal escribir_texto
    jal escribir_salto

    # CHECKSUM=<numero>
    move $a0, $s5
    la $a1, texto_checksum
    li $a2, 9
    jal escribir_texto
    move $a0, $s4
    jal entero_a_texto
    move $a0, $s5
    move $a1, $v0
    move $a2, $v1
    jal escribir_texto
    jal escribir_salto

    # STATUS=OK o STATUS=ERROR
    move $a0, $s5
    la $a1, texto_status
    li $a2, 7
    jal escribir_texto
    beq $s3, $zero, escribir_status_error

    move $a0, $s5
    la $a1, texto_ok
    li $a2, 2
    jal escribir_texto
    j cerrar_archivo

escribir_status_error:
    move $a0, $s5
    la $a1, texto_error
    li $a2, 5
    jal escribir_texto

cerrar_archivo:
    jal escribir_salto
    li $v0, 16
    move $a0, $s5
    syscall

    li $v0, 10
    syscall

escribir_texto:
    li $v0, 15
    syscall
    jr $ra

escribir_salto:
    move $a0, $s5
    la $a1, salto_linea
    li $a2, 1
    li $v0, 15
    syscall
    jr $ra

entero_a_texto:
    la $t0, numero_buffer
    addi $t0, $t0, 15
    li $t1, 0
    li $t4, 0

    slt $t4, $a0, $zero
    beq $t4, $zero, revisar_cero
    sub $a0, $zero, $a0

revisar_cero:
    bne $a0, $zero, convertir_loop
    addi $t0, $t0, -1
    li $t2, 48
    sb $t2, 0($t0)
    li $v0, 0
    add $v0, $v0, $t0
    li $v1, 1
    jr $ra

convertir_loop:
    li $t2, 10
    divu $a0, $t2
    mfhi $t3
    mflo $a0
    addi $t3, $t3, 48
    addi $t0, $t0, -1
    sb $t3, 0($t0)
    addi $t1, $t1, 1
    bne $a0, $zero, convertir_loop

    beq $t4, $zero, fin_entero_a_texto
    addi $t0, $t0, -1
    li $t2, 45
    sb $t2, 0($t0)
    addi $t1, $t1, 1

fin_entero_a_texto:
    li $v0, 0
    add $v0, $v0, $t0
    move $v1, $t1
    jr $ra




