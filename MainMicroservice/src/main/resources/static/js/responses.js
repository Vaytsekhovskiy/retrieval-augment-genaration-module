// Обработчик для открытия/закрытия меню
const userPanel = document.getElementById("userPanel");
userPanel.addEventListener("click", function (e) {
  // Проверяем, был ли клик на самом переключателе или его элементах
  const isToggleSwitch = e.target.closest(".slider");

  // Если клик был не на переключателе - обрабатываем открытие/закрытие
  if (!isToggleSwitch) {
    e.stopPropagation();
    this.classList.toggle("open");
  }
});

// // Обработчик для переключателя автоудаления
// const toggleSwitch = document.querySelector('.toggle-switch input');
// toggleSwitch.addEventListener('click', function(e) {
//     e.stopPropagation(); // Предотвращаем всплытие события
//     // Здесь можно добавить логику переключения
//     console.log('Автоудаление:', this.checked);
// });

// Закрываем меню при клике вне его
document.addEventListener("click", function () {
  userPanel.classList.remove("open");
});

// Обработчики для переключения размера шрифта
document.addEventListener('DOMContentLoaded', function() {
  // Элементы, размер шрифта которых нужно менять
  const dynamicElements = document.querySelectorAll('.response-block, .dynamic-font');
  const fontSizeBtns = document.querySelectorAll(".font-size-btn");

  // Функция для изменения размера шрифта
  function changeFontSize(size) {
    dynamicElements.forEach(el => {
      el.style.fontSize = size;
    });
    localStorage.setItem('selectedFontSize', size);
  }

  // Обработчик клика для кнопок
  fontSizeBtns.forEach((btn) => {
    btn.addEventListener("click", function(e) {
      console.log("Сработал обработчик переключателя шрифта");
      e.stopPropagation();
      
      // Обновляем активную кнопку
      fontSizeBtns.forEach((b) => b.classList.remove("active"));
      this.classList.add("active");

      // Определяем размер
      let size;
      switch (this.textContent.trim()) {
        case 'S': size = '14px'; break;
        case 'M': size = '16px'; break;
        case 'L': size = '18px'; break;
        default: size = '16px';
      }

      // Применяем размер
      changeFontSize(size);
      console.log("Выбран размер шрифта:", size);
    });
  });

  // Восстановление размера при загрузке
  const savedSize = localStorage.getItem('selectedFontSize');
  if (savedSize) {
    changeFontSize(savedSize);
    
    // Активируем соответствующую кнопку
    fontSizeBtns.forEach(btn => {
      btn.classList.remove("active");
      const btnSize = btn.textContent.trim();
      if (
        (btnSize === 'S' && savedSize === '14px') ||
        (btnSize === 'M' && savedSize === '16px') ||
        (btnSize === 'L' && savedSize === '18px')
      ) {
        btn.classList.add('active');
      }
    });
  }
});

function setSearchText(text) {
  document.getElementById("searchInput").value = text;
  // Можно автоматически запускать поиск:
  // document.querySelector('.search-button').click();
}

const isVisible = (element) => {
  if (!element) return false;
  const style = window.getComputedStyle(element);
  return style.display !== "none" && 
         style.visibility !== "hidden" && 
         style.opacity !== "0";
};


