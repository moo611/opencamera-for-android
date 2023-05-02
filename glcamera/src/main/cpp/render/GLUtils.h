/**
 *
 * Created by 公众号：字节流动 on 2021/3/16.
 * https://github.com/githubhaohao/OpenGLCamera2
 * 最新文章首发于公众号：字节流动，有疑问或者技术交流可以添加微信 Byte-Flow ,领取视频教程, 拉你进技术交流群
 *
 * */

#ifndef _BYTE_FLOW_GL_UTILS_H_
#define _BYTE_FLOW_GL_UTILS_H_

#include <GLES3/gl3.h>
#include <string>
#include <glm.hpp>

#define MATH_PI 3.141592653

class GLUtils {
public:
	static GLuint LoadShader(GLenum shaderType, const char *pSource);

	static GLuint CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource,
								GLuint &vertexShaderHandle,
								GLuint &fragShaderHandle);

	static GLuint CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource);

	static void DeleteProgram(GLuint &program);

	static void CheckGLError(const char *pGLOperation);

	static void setBool(GLuint programId, const std::string &name, bool value) {
		glUniform1i(glGetUniformLocation(programId, name.c_str()), (int) value);
	}

	static void setInt(GLuint programId, const std::string &name, int value) {
		glUniform1i(glGetUniformLocation(programId, name.c_str()), value);
	}

	static void setFloat(GLuint programId, const std::string &name, float value) {
		glUniform1f(glGetUniformLocation(programId, name.c_str()), value);
	}

	static void setVec2(GLuint programId, const std::string &name, const glm::vec2 &value) {
		glUniform2fv(glGetUniformLocation(programId, name.c_str()), 1, &value[0]);
	}

	static void setVec2(GLuint programId, const std::string &name, float x, float y) {
		glUniform2f(glGetUniformLocation(programId, name.c_str()), x, y);
	}

	static void setVec3(GLuint programId, const std::string &name, const glm::vec3 &value) {
		glUniform3fv(glGetUniformLocation(programId, name.c_str()), 1, &value[0]);
	}

	static void setVec3(GLuint programId, const std::string &name, float x, float y, float z) {
		glUniform3f(glGetUniformLocation(programId, name.c_str()), x, y, z);
	}

	static void setVec4(GLuint programId, const std::string &name, const glm::vec4 &value) {
		glUniform4fv(glGetUniformLocation(programId, name.c_str()), 1, &value[0]);
	}

	static void setVec4(GLuint programId, const std::string &name, float x, float y, float z, float w) {
		glUniform4f(glGetUniformLocation(programId, name.c_str()), x, y, z, w);
	}

	static void setVecN(GLuint programId, const std::string &name, int count, const float *values) {
		glUniform1fv(glGetUniformLocation(programId, name.c_str()), count, values);
	}

	static void setMat2(GLuint programId, const std::string &name, const glm::mat2 &mat) {
		glUniformMatrix2fv(glGetUniformLocation(programId, name.c_str()), 1, GL_FALSE, &mat[0][0]);
	}

	static void setMat3(GLuint programId, const std::string &name, const glm::mat3 &mat) {
		glUniformMatrix3fv(glGetUniformLocation(programId, name.c_str()), 1, GL_FALSE, &mat[0][0]);
	}

	static void setMat4(GLuint programId, const std::string &name, const glm::mat4 &mat) {
		glUniformMatrix4fv(glGetUniformLocation(programId, name.c_str()), 1, GL_FALSE, &mat[0][0]);
	}

	static glm::vec3 texCoordToVertexCoord(glm::vec2 texCoord) {
		return glm::vec3(2 * texCoord.x - 1, 1 - 2 * texCoord.y, 0);
	}

};

#endif // _BYTE_FLOW_GL_UTILS_H_