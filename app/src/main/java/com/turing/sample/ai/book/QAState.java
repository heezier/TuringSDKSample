package com.turing.sample.ai.book;

public enum QAState {
    /**
     * 退出QA状态
     */
    None,
    /**
     * 正在提问
     */
    Questing,
    /**
     * 正在回答
     */
    Answering,
    /**
     * 重新提问
     */
    ReQuesting,
    /**
     * 重新回答
     */
    ReAnswering,
    /**
     * 回答正确
     */
    AnswerCorrect,
    /**
     * 回答错误
     */
    AnswerWrong
}
