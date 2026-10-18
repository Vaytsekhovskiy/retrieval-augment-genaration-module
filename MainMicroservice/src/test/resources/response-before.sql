DELETE FROM responses;

insert into responses(id, ai_answer, answer, moderated, name, user_id, eval) VALUES
    (1, 'ai1', 'first', false, 'drive', 1, 0),
    (2, 'ai2', 'second', false, 'hola', 1, 2),
    (3, 'ai3', 'third', false, 'hi', 1, 3),
    (4, 'ai4', 'fourth', false, 'privet', 1, 4);