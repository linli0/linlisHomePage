import json
import requests
import tiktoken
import ollama

class LocalChatBot:
    # 初始化
    def __init__(
                self,
                api_key: str,
                model: str = "deepseek-r1:8b",
                proxy: str = None,
                max_tokens: int = 3000,
                temperature: float = 0.5,
                top_p: float = 1.0,
                presence_penalty: float = 0.0,
                frequency_penalty: float = 0.0,
                reply_count: int = 1,
                system_prompt: str = "你是DeepSeek",
    ) -> None:
        self.model = model
        self.api_key = api_key

        self.system_prompt = system_prompt
        self.max_tokens = max_tokens
        self.temperature = temperature
        self.top_p = top_p
        self.presence_penalty = presence_penalty
        self.frequency_penalty = frequency_penalty
        self.reply_count = reply_count

        self.sentence = ""
        self.temp = ""
        self.has_printed = False

        self.conversation: dict = {
            "default": [
                {
                    "role": "system",
                    "content": system_prompt,
                },
            ],
        }
    
    # 请求本地模型
    def ask_stream(
            self,
            prompt: str,
            lock=None,
            stop_event=None,
            role: str = "user",
            convo_id: str = "default",
    ) -> None:
        self.has_printed = False
        stream = ollama.generate(
        stream=True,
        model=self.model,
        prompt=prompt,
        )
        print('-----------------------------------------')
        full_response: str = ""
        for chunk in stream:
            if not chunk['done']:
                print(chunk['response'], end='', flush=True)
                full_response += chunk['response']
            else:
                print('\n')
                print('-----------------------------------------')
                print(f'总耗时：{chunk['total_duration']}')
                print('-----------------------------------------')
        self.has_printed = True
        self.sentence += full_response