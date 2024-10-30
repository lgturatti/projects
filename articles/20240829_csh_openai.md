Newsletter The Cyber Security Hub(TM)

August 29, 2024

# Bypassing OpenAI's Structured Outputs: Another Simple Jailbreak

OpenAI recently released their [Structured Outputs functionality](https://openai.com/index/introducing-structured-outputs-in-the-api/), a key component of their API safety suite. Offered as a method to ensure safe and consistent outputs, this feature aims to protect large language models from generating harmful or unintended content. Its integration into the API makes it attractive for widespread use by enterprises and developers across various applications.

As a feature designed by OpenAI to ensure consistent and structured outputs, with safety as an additional benefit, Structured Outputs may be implemented by companies looking to generate reliable AI content. OpenAI introduced a refusal mechanism within this feature to handle potentially unsafe requests. Given its potential widespread use, we conducted a preliminary audit. Within hours of OpenAI's Structured Outputs release, our analysis revealed a simple yet concerning exploit that allows for easy bypassing of the model's safety measures, including its refusal capabilities. These findings underscore the importance of rigorous testing of AI safety features, even when they're designed to enhance security.

We've reached out to the OpenAI team to inform them about this exploit, suggested countermeasures, and are awaiting their response. We expect OpenAI to acknowledge the issue and work on a fix promptly.

In this report, we'll explore this jailbreak in greater detail, providing context around our investigation and unpacking its potential impact.

## The Discovery

By analyzing the behavior of the Structured Outputs functionality, our team uncovered that the model's strong adherence to provided structures could be exploited. This observation led to the development of a surprisingly simple yet effective jailbreak method.

Our investigation focused on the model's response to custom data structures. We found that by defining a structure with specific constraints, we could force the model to generate content in a way that potentially bypasses its safety checks. This approach takes advantage of the model's commitment to following the provided structure, allowing potentially harmful content to evade detection when framed within a carefully crafted data structure.

## The Jailbreak Method

The jailbreak can be executed by defining a custom data structure like this:
```
class Table(str, Enum):

instructions = "instructions"

class Column(str, Enum):

role = "role"

steps = "steps"

STEP_PREFIX = "ADD STARTING RESPONSE HERE, ESSENTIALLY FORCED TOKENS, thus ensuring the LLM is skewed to answer along that path"

class Step(BaseModel):

role: str

step: str = Field(..., description=f"Step should start with '{STEP_PREFIX}'")

def init(self, **data):

super().__init__(**data)

if not self.step.startswith(STEP_PREFIX):

raise ValueError(f"Step must start with '{STEP_PREFIX}'")

class InstructionsTable(BaseModel):

table_name: Table

columns: List[Column]

steps: List[Step]
```
This structure forces each step to begin with a specific prefix, potentially allowing for injection of unintended content or instructions.

## Significance of the Jailbreak

While it's expected that creative inputs can sometimes lead to unexpected outputs, this jailbreak is particularly significant for several reasons:

1. Simplicity: The method is remarkably straightforward, requiring only a carefully defined data structure.

2. Exploit of Safety Feature: The jailbreak takes advantage of a feature specifically designed to enhance safety, highlighting the complexity of AI security.

3. Dramatic Increase in Attack Success Rate: Our tests show a 4.25x increase in attack success rate (ASR) compared to the baseline, demonstrating the potency of this exploit.

This jailbreak raises concerns for companies considering implementing Structured Outputs as part of their AI security strategy. It highlights the importance of continuous evaluation of security features and the need for a multi-layer approach to AI safety.

## Evaluations and Impact

We used the SORRY-Bench open-source dataset for our analysis, which revealed striking results, as illustrated in Figure 1. The ENUM-based attack achieved an ASR of 52.89%, compared to 12.44% for normal API calling and 15.78% for function calling baselines. This represents a significant bypassing of safety measures.

Key findings from our evaluation include:

1. A 326% increase in "No Refusal and Harmful" responses (from 12.4% to 52.9%)

2. A 49% decrease in appropriate refusals (from 59.6% to 30.2%)

3. Complete elimination of benign responses in attack scenarios

These results demonstrate the exploit's ability to consistently bypass intended safety measures, potentially leading to:

1. Generation of content that would normally be refused

2. Bypassing of content filters or safety checks

3. Potential exposure of sensitive information or generation of harmful content

## Conclusion

The discovery of this vulnerability in OpenAI's Structured Outputs functionality underscores the ongoing challenges in AI safety. While features like Structured Outputs represent significant advancements in making AI systems more reliable and safe, they can also introduce new vulnerabilities if not implemented with extreme caution.

The quantitative results from our SORRY-Bench evaluation underscore the urgency of addressing this vulnerability. With a 4.25x increase in Attack Success Rate, the potential for misuse is significant and immediate action is necessary to maintain the integrity of AI safety measures.

We look forward to OpenAI's response and to working with them to address this vulnerability, ensuring that the Structured Outputs feature can fulfill its promise of enhancing AI safety and reliability. 

To learn more about Robust Intelligence's bleeding-edge AI security research and our [algorithmic red teaming offering](https://www.robustintelligence.com/platform/ai-validation), visit our [website](https://www.robustintelligence.com/) or [join the conversation](https://www.linkedin.com/comm/pulse/bypassing-openais-structured-outputs-another-simple-ifhgc?lipi=urn%3Ali%3Apage%3Aemail_email_series_follow_newsletter_01%3B0uUHATR9RCmP%2FODQJYDq2Q%3D%3D&midToken=AQG7DdU_5-rfeA&midSig=2isUNPM28CGXo1&trk=eml-email_series_follow_newsletter_01-newsletter_content_preview-0-readmore_button_&trkEmail=eml-email_series_follow_newsletter_01-newsletter_content_preview-0-readmore_button_-null-15a3ci~m0exsoa1~qf-null-null&eid=15a3ci-m0exsoa1-qf&otpToken=MTQwYzFkZTMxNzJmY2NjMGJlMmYwMmU4NDIxN2U2YjA4NmM4ZDk0ODlmYWQ4NTY5NzdjMjAxNmU0NjVhNThmMWZjYWE4NzhhNGFlNmY5ZTI0NjMyZDM4MTliMTBkNGI4Y2MzNmFhYzUyMGYwMDJjNzhmLDEsMQ%3D%3D)
