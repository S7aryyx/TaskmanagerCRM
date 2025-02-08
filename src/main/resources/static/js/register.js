document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ username, password })
        });
        const data = await response.text();
        // Показываем уведомление (например, в alertBox)
        const alertBox = document.getElementById('alertBox');
        alertBox.innerText = data;
        alertBox.className = 'alert success';
        alertBox.style.display = 'block';
        setTimeout(() => { alertBox.style.display = 'none'; }, 3000);
        window.location.href = '/login.html';
    } catch (error) {
        console.error("Ошибка регистрации:", error);
        const alertBox = document.getElementById('alertBox');
        alertBox.innerText = "Ошибка при регистрации!";
        alertBox.className = 'alert error';
        alertBox.style.display = 'block';
        setTimeout(() => { alertBox.style.display = 'none'; }, 3000);
    }
});
