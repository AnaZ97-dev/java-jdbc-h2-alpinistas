-- Borrado de tablas por si ya existen
DROP TABLE IF EXISTS Ascensiones ;
DROP TABLE IF EXISTS Picos ;
DROP TABLE IF EXISTS Alpinistas ;

--
-- Estructura de las tablas
--

-- Tabla Picos
CREATE TABLE Picos (
    idPico INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    cordillera VARCHAR(100),
    localizacion VARCHAR(100),
    altitud INT
);

-- Tabla Alpinistas
CREATE TABLE Alpinistas (
    idAlpinista INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(50),
    fechaNacimiento DATE
);

-- Tabla Ascensiones
CREATE TABLE Ascensiones (
    idAscension INT AUTO_INCREMENT PRIMARY KEY,
    idPico INT,
    idAlpinista INT,
    fecha DATE,
    ruta VARCHAR(100),
    CONSTRAINT fk_pico FOREIGN KEY (idPico) REFERENCES Picos(idPico) 
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_alpinista FOREIGN KEY (idAlpinista) REFERENCES Alpinistas(idAlpinista) 
        ON UPDATE CASCADE ON DELETE CASCADE
);