CREATE USER mini_football_db_manager WITH PASSWORD "Password";
CREATE DATABASE mini_football_db OWNER mini_football_db_manager;
 \c mini_football_db;
GRANT ALL PRIVILEGES ON DATABASE mini_football_db TO mini_football_db_manager;
GRANT ALL ON SCHEMA public TO mini_football_db_manager;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_football_db_manager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mini_football_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO mini_football_db_manager;
CREATE TYPE player_position AS ENUM ('GK', 'DEF', 'MIDF', 'STR');
CREATE TYPE continent_type AS ENUM ('AFRICA', 'EUROPA', 'ASIA', 'AMERICA');

