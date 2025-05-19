.global _start
.global _main
.text
_start:
  call _main
  mov %eax, %edi
  mov $60, %rax
  syscall

_main:
  movl $7, %eax
  movl $5, %ebx
  movl $6, %ecx
  movl %ebx, %edx
  imull %ecx, %edx
  movl %eax, %esi
  addl %edx, %esi
  movl %esi, %eax
  ret
