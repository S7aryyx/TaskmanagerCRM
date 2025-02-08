// Функция для получения задач
async function fetchTasks() {
    const token = localStorage.getItem('8MgKJ+yjWiJxIad/P0HNlWVElcCIlh8C/YeExgLtKJw=');
    if (!token) {
        window.location.href = '/login.html'; // Перенаправляем на страницу логина, если токен отсутствует
        return;
    }

    try {
        const response = await fetch('/api/tasks', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!response.ok) {
            throw new Error('Ошибка при получении задач');
        }

        const tasks = await response.json();
        let html = '';
        tasks.forEach(task => {
            html += `<div>
                      <h3>${task.title}</h3>
                      <p>${task.description}</p>
                      <p>Status: ${task.status}</p>
                      <p>Deadline: ${task.deadline}</p>
                    </div>`;
        });

        document.getElementById('taskList').innerHTML = html;
    } catch (error) {
        document.getElementById('taskList').innerHTML = `<p style="color: red;">${error.message}</p>`;
    }
}

// Функция для выхода
function logout() {
    localStorage.removeItem('jwt');
    window.location.href = '/login.html';
}

// Функция добавления новой задачи
document.getElementById('add-task-form').addEventListener('submit', async function(event) {
    event.preventDefault();

    const token = localStorage.getItem('jwt');
    if (!token) {
        alert("Вы не авторизованы");
        window.location.href = '/login.html';
        return;
    }

    const title = document.getElementById('title').value;
    const description = document.getElementById('description').value;
    const deadline = document.getElementById('deadline').value;

    try {
        const response = await fetch('/api/tasks', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title,
                description,
                status: "NEW", // Статус по умолчанию
                deadline
            })
        });

        if (response.ok) {
            alert('Задача успешно добавлена!');
            fetchTasks();  // Обновить список задач
        } else {
            throw new Error('Ошибка при добавлении задачи');
        }
    } catch (error) {
        alert(error.message);
    }
});

// Загрузка задач при загрузке страницы
fetchTasks();
