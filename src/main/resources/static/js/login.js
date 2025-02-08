document.addEventListener("DOMContentLoaded", function () {
    document.getElementById('loginForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        console.log("Submitting login form with username:", username); // Логирование при отправке формы

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Login failed:", errorText); // Логирование ошибки входа
                throw new Error(errorText || 'Ошибка входа');
            }

            const data = await response.json();
            console.log("Login response data:", data); // Логирование полученных данных

            if (data.token) {
                // Сохраняем токен в localStorage
                localStorage.setItem('jwt', data.token);

                // Логируем роль для проверки
                console.log("User role from server:", data.role); // Логирование роли

                // Логика для перенаправления
                if (data.role === 'ROLE_ADMIN') {
                    console.log('Redirecting to admin page'); // Логирование перенаправления
                    window.location.href = '/admin.html';  // Перенаправляем на страницу администратора
                } else {
                    console.log('Redirecting to tasks page'); // Логирование перенаправления
                    window.location.href = '/tasks.html';  // Перенаправляем на страницу задач для обычных пользователей
                }
            } else {
                console.error('Token not received'); // Логирование ошибки, если нет токена
                throw new Error('Не получен токен');
            }
        } catch (error) {
            alert(error.message);  // Показываем ошибку, если она возникла
            console.error("Error during login:", error); // Логирование ошибки в консоль
        }
    });
});