const togglePassword = document.getElementById('togglePassword');
const passwordInput = document.getElementById('password');


const eyeIcons = {
    open: `<path d="M12 5C5 5 1 12 1 12C1 12 5 19 12 19C19 19 23 12 23 12C23 12 19 5 12 5Z" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>`,
   closed: `<path d="M12 5C5 5 1 12 1 12C1 12 5 19 12 19C19 19 23 12 23 12C23 12 19 5 12 5Z" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M2 2L22 22" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round"/>`
}
togglePassword.addEventListener('click', function () {
    // Меняем тип поля ввода
    const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
    passwordInput.setAttribute('type', type);

    // Меняем иконку (опционально)
    if (type === 'text') {
        togglePassword.innerHTML = eyeIcons.closed;
    } else {
        togglePassword.innerHTML = eyeIcons.open;
    }
});