import os
from PySide6.QtWidgets import (
    QApplication, QWidget, QVBoxLayout, QLabel, QLineEdit,
    QPushButton, QMessageBox
)
from PySide6.QtGui import QFont

ASCII_ART = r"""
███╗   ██╗ ██████╗ ██╗   ██╗ █████╗ 
████╗  ██║██╔═══██╗██║   ██║██╔══██╗
██╔██╗ ██║██║   ██║██║   ██║███████║
██║╚██╗██║██║   ██║██║   ██║██╔══██║
██║ ╚████║╚██████╔╝╚██████╔╝██║  ██║
╚═╝  ╚═══╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═╝
        N   O   V   A
"""

ENV_PATH = ".env"

def save_env(api_key, portfolio):
    with open(ENV_PATH, "w") as f:
        f.write(f"GEMINI_API_KEY={api_key}\n")
        f.write(f"PORTFOLIO={portfolio}\n")


def run_setup():
    app = QApplication([])

    window = QWidget()
    window.setWindowTitle("NOVA Setup Wizard")
    window.resize(600, 520)

    layout = QVBoxLayout()

    # ASCII art display
    ascii_label = QLabel(ASCII_ART)
    ascii_label.setFont(QFont("Courier", 11))
    layout.addWidget(ascii_label)

    # API label + input
    api_label = QLabel("Enter your Gemini API key:")
    api_label.setFont(QFont("Arial", 12))
    api_input = QLineEdit()

    layout.addWidget(api_label)
    layout.addWidget(api_input)

    # Portfolio label + input
    portfolio_label = QLabel("Enter your portfolio (AAPL, MSFT, NVDA):")
    portfolio_label.setFont(QFont("Arial", 12))
    portfolio_input = QLineEdit()

    layout.addWidget(portfolio_label)
    layout.addWidget(portfolio_input)

    # Button action
    def save():
        api_key = api_input.text().strip()
        portfolio = portfolio_input.text().strip()

        if not api_key or not portfolio:
            QMessageBox.warning(window, "Error", "All fields must be filled out.")
            return

        save_env(api_key, portfolio)
        QMessageBox.information(window, "Success", "Setup complete! .env created.")
        window.close()

    # Save button
    save_btn = QPushButton("Save Configuration")
    save_btn.setFont(QFont("Arial", 12))
    save_btn.clicked.connect(save)

    layout.addWidget(save_btn)

    window.setLayout(layout)
    window.show()
    app.exec()


if __name__ == "__main__":
    run_setup()
